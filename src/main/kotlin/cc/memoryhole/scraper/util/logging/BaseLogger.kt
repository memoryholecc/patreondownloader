package cc.memoryhole.scraper.util.logging

import com.andreapivetta.kolor.Color
import java.lang.Exception

abstract class BaseLogger {
    enum class SECTIONS(val text: String, val color: Color) {
        GENERAL("General", Color.WHITE),
        MINIO("MinIO", Color.LIGHT_BLUE),
        THREADMANAGER("Thread-Manager", Color.LIGHT_YELLOW),
        RESTSERVICE("Rest-Service", Color.CYAN),
        BACKEND("Backend", Color.LIGHT_MAGENTA),
        TASK("Task", Color.YELLOW),
        PATREON("Patreon", Color.LIGHT_RED),
        SCRAPER("Scraper", Color.BLUE),
    }

    abstract fun log(section: SECTIONS, message: String)
    abstract fun log(section: SECTIONS, message: String, customMessageColor: Color?)
    abstract fun error(section: SECTIONS, message: String)
    abstract fun error(section: SECTIONS, exception: Exception)
}
