package cc.memoryhole.scraper.service

import cc.memoryhole.scraper.CONFIG
import cc.memoryhole.scraper.LOGGER
import cc.memoryhole.scraper.config.ScraperConfigSpec
import cc.memoryhole.scraper.util.logging.BaseLogger
import com.andreapivetta.kolor.Color
import io.minio.*

class MinIOService {
    var minioClient: MinioClient = MinioClient.builder()
        .endpoint(CONFIG[ScraperConfigSpec.MinIO.host])
        .credentials(CONFIG[ScraperConfigSpec.MinIO.accessKey], CONFIG[ScraperConfigSpec.MinIO.secretKey])
        .build()

    private val bucketName = CONFIG[ScraperConfigSpec.MinIO.bucketName]

    fun start() {
        LOGGER.log(BaseLogger.SECTIONS.MINIO, "Starting...")
        try {
            val found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())
            if (!found) {
                LOGGER.log(BaseLogger.SECTIONS.MINIO, "Bucked does not exist, creating new one")
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
            }
            LOGGER.log(BaseLogger.SECTIONS.MINIO, "Started")

        } catch (ex: Exception) {
            LOGGER.log(BaseLogger.SECTIONS.MINIO, "Error whilst starting service", Color.RED)
            LOGGER.log(BaseLogger.SECTIONS.MINIO, ex.message.toString(), Color.RED)
            LOGGER.log(BaseLogger.SECTIONS.MINIO, ex.cause?.message.toString(), Color.RED)
            LOGGER.log(BaseLogger.SECTIONS.MINIO, "Retrying in 10 seconds", Color.RED)

            Thread.sleep(1000 * 10)
            start()
        }
    }

    fun uploadFile(objectName: String, fileName: String): ObjectWriteResponse? {
        return minioClient.uploadObject(
            UploadObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .filename(fileName)
                .build()
        )
    }
}