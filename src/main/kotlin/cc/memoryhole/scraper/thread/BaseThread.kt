package cc.memoryhole.scraper.thread

import cc.memoryhole.scraper.util.logging.BaseLogger
import kotlin.concurrent.thread

open class BaseThread : Thread() {
    var running: Boolean = false
    var failed: Boolean = false
    lateinit var logger: BaseLogger
    lateinit var failedException: Exception

    fun startThread() {
        running = true
        thread {
            try {
                run()
            } catch (ex: Exception) {
                ex.printStackTrace()
                failedException = ex
                failed = true
            }
            running = false
        }
    }
}