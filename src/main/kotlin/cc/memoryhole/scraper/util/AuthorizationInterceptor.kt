package cc.memoryhole.scraper.util

import cc.memoryhole.scraper.CONFIG
import cc.memoryhole.scraper.config.ScraperConfigSpec
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", CONFIG[ScraperConfigSpec.Backend.apiKey])
            .build()

        return chain.proceed(request)
    }

}
