package cc.memoryhole.scraper.thread.impl.patreon

import cc.memoryhole.scraper.CONFIG
import cc.memoryhole.scraper.INSTANCE
import cc.memoryhole.scraper.config.ScraperConfigSpec
import cc.memoryhole.scraper.model.patreon.PatreonPost
import cc.memoryhole.scraper.thread.BaseThread
import cc.memoryhole.scraper.util.WebAPI
import cc.memoryhole.scraper.util.logging.BaseLogger
import cc.memoryhole.scraper.util.logging.ImportLogger
import com.andreapivetta.kolor.Color
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.minio.GetObjectArgs
import io.minio.errors.ErrorResponseException
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*

class PatreonPostDownloadThread(
    private val importId: UUID,
    private val patreonPost: PatreonPost
) : BaseThread() {
    private val webAPI = WebAPI()
    private val downloadPath = System.getProperty("user.dir") + "/download"

    init {
        priority = 1
    }


    override fun run() {
        val campaignId = patreonPost.postRelationships.postCampaign.patreonData.id

        val campaignFolder = File("${downloadPath}/${campaignId}/${patreonPost.id}")
        if (!campaignFolder.exists())
            campaignFolder.mkdirs()

        logger = ImportLogger(importId, campaignId)

        if (!CONFIG[ScraperConfigSpec.Debugging.skipMedia]) {
            val coverFile = File("$campaignFolder/cover.png")

            var coverExists = true;

            // get object given the bucket and object name
            try {
                INSTANCE.minIOService.minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(CONFIG[ScraperConfigSpec.MinIO.bucketName])
                        .`object`("$campaignId/${patreonPost.id}/${coverFile.name}")
                        .build()
                ).close()
            } catch (ex: ErrorResponseException) {
                if (ex.errorResponse().code().equals("NoSuchKey")) {
                    coverExists = false
                    //logger.log(BaseLogger.SECTIONS.MINIO, "File ($fileName) doesn't exist, downloading...")
                } else {
                    ex.printStackTrace()
                }
            }

            if (coverExists) {
                logger.log(BaseLogger.SECTIONS.PATREON, "Skipping Post Cover ${patreonPost.id}, because it was downloaded already")
            } else {

                // text, link, poll or embed posts dont have images
                logger.log(BaseLogger.SECTIONS.PATREON, "Downloading Post Cover: ${patreonPost.id}")


                if (patreonPost.attributes.postType != "text_only" && patreonPost.attributes.postImage != null && patreonPost.attributes.postImage.largeUrl.isNotEmpty()) {
                    webAPI.downloadToPath(
                        patreonPost.attributes.postImage.thumbUrl,
                        coverFile.parent,
                        coverFile.name
                    )
                }


                val infoFile = File("$campaignFolder/post.json")

                val node: JsonNode = ObjectMapper().valueToTree(patreonPost)

                infoFile.writeText(node.toPrettyString())

                /**
                 * MinIO File Upload
                 */

                if (coverFile.exists()) {
                    val coverResponse =
                        INSTANCE.minIOService.uploadFile(
                            "$campaignId/${patreonPost.id}/${coverFile.name}",
                            "$coverFile"
                        )
                    if (coverResponse != null)
                        coverFile.delete()
                }

                if (infoFile.exists()) {
                    try {
                        val infoResponse =
                            INSTANCE.minIOService.uploadFile(
                                "$campaignId/${patreonPost.id}/${infoFile.name}",
                                "$infoFile"
                            )
                        if (infoResponse != null)
                            infoFile.delete()
                    } catch (ex: IOException) {
                        println("ERROR: " + ex.message)
                        println("ON PATH: ${infoFile.path}")
                        println("ON POST: ${patreonPost.id}")
                        println("ON FILE: ${infoFile.name}")
                    }
                }
                if (infoFile.parentFile.isDirectory && !Files.list(infoFile.parentFile.toPath()).findAny().isPresent) {
                    infoFile.parentFile.delete()
                }
            }
        } else {
            logger.log(BaseLogger.SECTIONS.GENERAL, "Skipping media", Color.RED)
        }
    }
}