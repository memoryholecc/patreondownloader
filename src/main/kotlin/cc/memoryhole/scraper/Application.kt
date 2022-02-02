package cc.memoryhole.scraper

import cc.memoryhole.scraper.config.ScraperConfigSpec
import cc.memoryhole.scraper.graphql.CheckBackendQuery
import cc.memoryhole.scraper.service.MinIOService
import cc.memoryhole.scraper.service.RestService
import cc.memoryhole.scraper.task.DeleteOldLogFilesTask
import cc.memoryhole.scraper.thread.ThreadManager
import cc.memoryhole.scraper.util.AuthorizationInterceptor
import cc.memoryhole.scraper.util.WebAPI
import cc.memoryhole.scraper.util.logging.BaseLogger
import cc.memoryhole.scraper.util.logging.GenericLogger
import com.andreapivetta.kolor.Color
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

lateinit var INSTANCE: Application
val CONFIG = Config { addSpec(ScraperConfigSpec) }
    .from.env()
    .from.systemProperties()
    .from.yaml.file("config.yml", true)

fun main() {
    INSTANCE = Application()
}

val LOGGER = GenericLogger()

class Application {

    var minIOService: MinIOService
    var threadManager: ThreadManager
    private var restService: RestService
    var apolloClient: ApolloClient = ApolloClient.builder()
        .serverUrl(CONFIG[ScraperConfigSpec.Backend.endpoint])
        .okHttpClient(
            OkHttpClient.Builder()
                .addInterceptor(AuthorizationInterceptor())
                .build()
        )
        .build()
    var webApi = WebAPI()

    init {

        val time = measureTimeMillis {
            // connect to services first before starting threadmanager / web services

            minIOService = MinIOService()
            minIOService.start()

            runBlocking {
                checkBackend()
            }

            threadManager = ThreadManager()
            thread {
                threadManager.start()
            }

            restService = RestService()
            restService.start()

            Timer().schedule(DeleteOldLogFilesTask(), 0, TimeUnit.MINUTES.toMillis(30))
        }

        LOGGER.log(BaseLogger.SECTIONS.GENERAL, "Scraper started in ${time}ms")
    }

    private suspend fun checkBackend() {

        LOGGER.log(BaseLogger.SECTIONS.BACKEND, "Checking if backend is available...")
        val response = apolloQuery(CheckBackendQuery())
        if (response == null) {
            LOGGER.log(BaseLogger.SECTIONS.BACKEND, "Error whilst connecting to backend", Color.RED)
            LOGGER.log(BaseLogger.SECTIONS.BACKEND, "Retrying in 10 seconds", Color.RED)

            Thread.sleep(1000 * 10)
            checkBackend()
        } else {
            LOGGER.log(BaseLogger.SECTIONS.BACKEND, "Backend is available")
        }
    }

    //TODO: Refactor & move somewhere else
    suspend fun <D : Operation.Data, T, V : Operation.Variables> apolloQuery(
        query: Query<D, T, V>,
        supressErrors: Boolean = false
    ): Response<T>? {
        lateinit var queryResponse: Response<T>
        try {
            queryResponse = apolloClient.query(query).await()
        } catch (ex: ApolloException) {
            LOGGER.log(BaseLogger.SECTIONS.BACKEND, "Error whilst executing GraphQL Query", Color.RED)
            LOGGER.log(BaseLogger.SECTIONS.BACKEND, ex.message.toString(), Color.RED)
            LOGGER.log(BaseLogger.SECTIONS.BACKEND, ex.cause?.message.toString(), Color.RED)
            return null
        }

        /*
        if (queryResponse.hasErrors()) {
            if (!supressErrors)
                println(queryResponse.errors)
            return null
        }*/

        return queryResponse
    }

    suspend fun <D : Operation.Data, T, V : Operation.Variables> apolloMutation(mutation: Mutation<D, T, V>): Response<T>? {
        lateinit var mutationResponse: Response<T>
        try {
            mutationResponse = apolloClient.mutate(mutation)
                .await()
        } catch (ex: ApolloException) {
            LOGGER.log(BaseLogger.SECTIONS.BACKEND, "Error whilst executing GraphQL Mutation", Color.RED)
            LOGGER.log(BaseLogger.SECTIONS.BACKEND, ex.message.toString(), Color.RED)
            LOGGER.log(BaseLogger.SECTIONS.BACKEND, ex.cause?.message.toString(), Color.RED)
            return null
        }

        /*
        if (mutationRresponse.hasErrors()) {
            println(mutationRresponse.errors)
            return null
        }*/

        return mutationResponse
    }
}
