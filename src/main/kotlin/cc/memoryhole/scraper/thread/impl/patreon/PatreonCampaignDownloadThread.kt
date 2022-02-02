package cc.memoryhole.scraper.thread.impl.patreon

import cc.memoryhole.scraper.INSTANCE
import cc.memoryhole.scraper.model.patreon.PatreonCampaign
import cc.memoryhole.scraper.thread.BaseThread
import cc.memoryhole.scraper.util.Utils
import cc.memoryhole.scraper.util.WebAPI
import cc.memoryhole.scraper.util.logging.BaseLogger
import cc.memoryhole.scraper.util.logging.ImportLogger
import java.io.File
import java.nio.file.Files
import java.util.*

class PatreonCampaignDownloadThread(
    private var importId: UUID,
    private var patreonCampaign: PatreonCampaign,
) : BaseThread() {
    private val webAPI = WebAPI()
    private val utils = Utils()
    private val downloadPath = System.getProperty("user.dir") + "/download"


    init {
        priority = 1
    }

    override fun run() {
        val campaignId = patreonCampaign.id

        logger = ImportLogger(importId, campaignId.toString())

        val campaignFolder = File("$downloadPath/$campaignId")

        if (!campaignFolder.exists())
            campaignFolder.mkdirs()

        logger.log(BaseLogger.SECTIONS.PATREON, "Downloading Avatar")

        val avatarUrl = patreonCampaign.campaignAttributes.avatarPhotoUrl
        val avatarFileName = utils.guessNameFromURL(avatarUrl)
        val avatarFile = File("$campaignFolder/avatar.${avatarFileName.split(".").last()}")

        webAPI.downloadToPath(avatarUrl, campaignFolder.toString(), avatarFile.name)

        logger.log(BaseLogger.SECTIONS.PATREON, "Downloading Cover")

        val coverUrl = patreonCampaign.campaignAttributes.coverPhotoUrl
        val coverFileName = utils.guessNameFromURL(coverUrl)
        val coverFile = File("$campaignFolder/cover.${coverFileName.split(".").last()}")

        webAPI.downloadToPath(coverUrl, campaignFolder.toString(), coverFile.name)

        /**
         * MinIO File Upload
         */

        logger.log(BaseLogger.SECTIONS.MINIO, "Uploading Campaign Files")

        if (avatarFile.exists()) {
            val mediaResponse = INSTANCE.minIOService.uploadFile(
                "$campaignId/${avatarFile.name}",
                "$avatarFile"
            )
            if (mediaResponse != null)
                avatarFile.delete()
        }

        if (coverFile.exists()) {
            val infoResponse = INSTANCE.minIOService.uploadFile(
                "$campaignId/${coverFile.name}",
                "$coverFile"
            )
            if (infoResponse != null)
                coverFile.delete()
        }
        if (coverFile.parentFile.isDirectory && !Files.list(coverFile.parentFile.toPath()).findAny().isPresent) {
            coverFile.parentFile.delete()
        }
        logger.log(BaseLogger.SECTIONS.MINIO, "Finished Uploading & Cleaning")
    }
}