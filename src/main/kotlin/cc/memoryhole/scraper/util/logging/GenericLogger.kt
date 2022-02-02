package cc.memoryhole.scraper.util.logging

import com.andreapivetta.kolor.Color
import com.andreapivetta.kolor.Kolor
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GenericLogger : BaseLogger() {

    override fun log(section: SECTIONS, message: String) {
        log(section, message, null)
    }

    override fun log(section: SECTIONS, message: String, customMessageColor: Color?) {
        val coloredSectionPrefix = color(section.text, section.color)

        val coloredMessage =
            if (customMessageColor != null)
                color(message, customMessageColor)
            else
                message

        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))

        val logMessage =
            "${color(date, Color.LIGHT_GRAY)} ${color("|", Color.DARK_GRAY)} $coloredSectionPrefix ${
                color(">", Color.DARK_GRAY)
            } $coloredMessage"

        println(logMessage)
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