package cc.memoryhole.scraper.thread.impl.patreon

import cc.memoryhole.scraper.CONFIG
import cc.memoryhole.scraper.INSTANCE
import cc.memoryhole.scraper.config.ScraperConfigSpec
import cc.memoryhole.scraper.model.patreon.PatreonMedia
import cc.memoryhole.scraper.thread.BaseThread
import cc.memoryhole.scraper.util.Utils
import cc.memoryhole.scraper.util.WebAPI
import cc.memoryhole.scraper.util.logging.BaseLogger
import cc.memoryhole.scraper.util.logging.ImportLogger
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.minio.GetObjectArgs
import io.minio.errors.ErrorResponseException
import java.io.File
import java.nio.file.Files
import java.util.*


class PatreonMediaDownloadThread(
    private var importId: UUID,
    private var campaignId: String,
    private var patreonMedia: PatreonMedia
) : BaseThread() {
    private val webAPI = WebAPI()
    private val utils = Utils()
    private val downloadPath = System.getProperty("user.dir") + "/download"

    init {
        priority = 1
    }


    override fun run() {
        val postId = patreonMedia.mediaAttributes.ownerId
        val postFolder = File("$downloadPath/$campaignId/$postId")
        if (!postFolder.exists())
            postFolder.mkdirs()

        logger = ImportLogger(importId, campaignId)

        val fileName = patreonMedia.mediaAttributes.fileName
        val mediaFile = File("$postFolder/$fileName")

        // logger.log(BaseLogger.SECTIONS.MINIO, "Checking if file ($fileName) exists")

        var fileExists = true;

        // get object given the bucket and object name
        try {
            INSTANCE.minIOService.minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(CONFIG[ScraperConfigSpec.MinIO.bucketName])
                    .`object`("$campaignId/$postId/$fileName")
                    .build()
            ).close()
        } catch (ex: ErrorResponseException) {
            if (ex.errorResponse().code().equals("NoSuchKey")) {
                fileExists = false
                //logger.log(BaseLogger.SECTIONS.MINIO, "File ($fileName) doesn't exist, downloading...")
            } else {
                ex.printStackTrace()
            }
        }

        if (fileExists) {
            logger.log(BaseLogger.SECTIONS.PATREON, "Skipping file ($fileName), because it was downloaded already.")
        } else {
            logger.log(
                BaseLogger.SECTIONS.PATREON,
                "Downloading Media: ${patreonMedia.id} (Post: $postId, Filename: $fileName, Size: ${
                    utils.humanReadableByteCountSI(
                        patreonMedia.mediaAttributes.sizeBytes
                    )
                })"
            )


            webAPI.downloadToPath(patreonMedia.mediaAttributes.downloadUrl, postFolder.toString(), mediaFile.name)

            val infoFile = File("$postFolder/${patreonMedia.id}.json")
            val node: JsonNode = ObjectMapper().valueToTree(patreonMedia)

            infoFile.writeText(node.toPrettyString())

            /**
             * MinIO File Upload
             */

            //println("$logPrefix > Uploading to MinIO: $postId")

            // some errors were caused if the minio upload would happen while the fs still struggled with the download
            sleep(1000)

            if (mediaFile.exists()) {
                val mediaResponse = INSTANCE.minIOService.uploadFile(
                    "$campaignId/$postId/${mediaFile.name}",
                    "$mediaFile"
                )
                if (mediaResponse != null)
                    mediaFile.delete()
            }

            if (infoFile.exists()) {
                val infoResponse = INSTANCE.minIOService.uploadFile(
                    "$campaignId/$postId/${infoFile.name}",
                    "$infoFile"
                )
                if (infoResponse != null)
                    infoFile.delete()
            }
            if (infoFile.parentFile.isDirectory && !Files.list(infoFile.parentFile.toPath()).findAny().isPresent) {
                infoFile.parentFile.delete()
            }
            //println("$logPrefix > Finished Uploading & Cleaning: $postId")
        }
    }
}