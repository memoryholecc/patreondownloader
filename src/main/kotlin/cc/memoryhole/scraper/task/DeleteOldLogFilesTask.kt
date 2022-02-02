package cc.memoryhole.scraper.task

import cc.memoryhole.scraper.LOGGER
import cc.memoryhole.scraper.util.logging.BaseLogger
import cc.memoryhole.scraper.util.logging.LOG_DIRECTORY
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists

val deletionTime: Long = TimeUnit.DAYS.toMillis(7)

class DeleteOldLogFilesTask : TimerTask() {
    override fun run() {
        LOGGER.log(BaseLogger.SECTIONS.TASK, "Checking for old log files")
        if (LOG_DIRECTORY.exists()) {
            for (file in LOG_DIRECTORY.toFile().walk()) {
                val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)

                if (System.currentTimeMillis() - attr.creationTime().toMillis() > deletionTime) {
                    LOGGER.log(BaseLogger.SECTIONS.TASK, "Deleting Log File: $file")
                    file.delete()
                }
            }
        }
        LOGGER.log(BaseLogger.SECTIONS.TASK, "Finished checking for old log files")
    }
}