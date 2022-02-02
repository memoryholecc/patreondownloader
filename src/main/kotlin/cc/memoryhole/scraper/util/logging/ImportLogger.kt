package cc.memoryhole.scraper.util.logging

import com.andreapivetta.kolor.Color
import com.andreapivetta.kolor.Kolor
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createDirectory

val LOG_DIRECTORY = Path("./log")

class ImportLogger(importId: UUID, val campaignId: String) : BaseLogger() {

    private val logFile = File("$LOG_DIRECTORY/$importId.log")
    private var bufferedWriter: BufferedWriter

    init {
        if (!logFile.parentFile.exists()) {
            LOG_DIRECTORY.createDirectory()
        }
        if (!logFile.exists())
            logFile.createNewFile()

        bufferedWriter = FileOutputStream(logFile, true).bufferedWriter()
    }

    override fun log(section: SECTIONS, message: String) {
        log(section, message, null)
    }

    override fun log(section: SECTIONS, message: String, customMessageColor: Color?) {
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))

        val logPrefix = "$date | $campaignId"
        val coloredLogPrefix =
            "${color(date, Color.LIGHT_GRAY)} ${color("|", Color.DARK_GRAY)} ${color(campaignId, Color.LIGHT_GRAY)}"

        val sectionPrefix = section.text
        val coloredSectionPrefix = color(section.text, section.color)

        val coloredMessage =
            if (customMessageColor != null)
                color(message, customMessageColor)
            else
                message

        val logMessage =
            "$coloredLogPrefix ${color("@", Color.DARK_GRAY)} $coloredSectionPrefix ${
                color(">", Color.DARK_GRAY)
            } $coloredMessage"

        println(logMessage)


        val plainTextLogMessage = "$date | $logPrefix @ $sectionPrefix > $message"

        logFile.appendText(plainTextLogMessage + "\n")
    }

    override fun error(section: SECTIONS, message: String) {
        log(section, message, Color.RED)
    }

    override fun error(section: SECTIONS, exception: Exception) {
        error(section, exception.message.toString())
        error(section, exception.cause?.message.toString())
    }

    private fun color(string: String, color: Color): String {
        return Kolor.foreground(string, color)
    }
}