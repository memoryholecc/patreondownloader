package cc.memoryhole.scraper.config

import com.uchuhimo.konf.ConfigSpec

object ScraperConfigSpec : ConfigSpec("") {
    val env by optional("prod")

    val threads by required<Int>()

    object Debugging : ConfigSpec("debugging") {
        val skipMedia by optional(false)
        val skipComments by optional(false)
    }

    object MinIO : ConfigSpec("minio") {
        val host by required<String>()
        val publicEndpoint by required<String>()
        val accessKey by required<String>()
        val secretKey by required<String>()
        val bucketName by required<String>()
    }

    object Rest : ConfigSpec("rest") {
        val port by required<Int>()
    }

    object Backend : ConfigSpec("backend") {
        val endpoint by required<String>()
        val apiKey by required<String>()
    }
}