package cc.memoryhole.scraper.service

import cc.memoryhole.scraper.CONFIG
import cc.memoryhole.scraper.INSTANCE
import cc.memoryhole.scraper.LOGGER
import cc.memoryhole.scraper.config.ScraperConfigSpec
import cc.memoryhole.scraper.service.services.EnqueueService
import cc.memoryhole.scraper.service.services.LookupService
import cc.memoryhole.scraper.util.logging.BaseLogger
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*


class RestService {

    private var app: Javalin = Javalin.create {
        it.enableCorsForAllOrigins()
        it.showJavalinBanner = false
    }


    init {
        app.get("/") { ctx -> ctx.result("Hello World") }

        app.routes {
            path("/api/v1") {
                path("/lookup") {
                    post("/{service}", LookupService().perform)
                }
                path("/enqueue") {
                    post("/{service}", EnqueueService().perform)
                }
            }
        }
    }

    fun start() {
        LOGGER.log(BaseLogger.SECTIONS.RESTSERVICE, "Started")
        app.start(CONFIG[ScraperConfigSpec.Rest.port])
    }
}