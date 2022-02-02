package cc.memoryhole.scraper.thread

import cc.memoryhole.scraper.CONFIG
import cc.memoryhole.scraper.LOGGER
import cc.memoryhole.scraper.config.ScraperConfigSpec
import cc.memoryhole.scraper.util.logging.BaseLogger
import com.andreapivetta.kolor.Color
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentLinkedQueue

class ThreadManager() {
    private var running: Boolean = true

    private var threads: Int = CONFIG[ScraperConfigSpec.threads]

    private var concurrentThreadQueue = ConcurrentLinkedQueue<BaseThread>()

    private var runningThreads = ConcurrentLinkedQueue<BaseThread>()

    // TODO: implement this as well as auto retrys (using different proxies)
    private var failedThreads = ConcurrentLinkedQueue<BaseThread>()

    // private var processingQueue: MutableList<BaseThread> = Collections.synchronizedList(mutableListOf<BaseThread>())

    private var finishedThreads = 0

    fun start() {
        LOGGER.log(BaseLogger.SECTIONS.THREADMANAGER, "Started")
        while (running) {
            if (concurrentThreadQueue.size > 0 && runningThreads.size < threads) {
                val thread: BaseThread = concurrentThreadQueue.poll()
                thread.startThread()
                runningThreads.add(thread)
            }

            if (runningThreads.isNotEmpty()) {
                for (runningThread in runningThreads) {
                    if (!runningThread.running) {
                        runningThreads.remove(runningThread)

                        if (runningThread.failed) {
                            failedThreads.add(runningThread)
                            runningThread.logger.log(BaseLogger.SECTIONS.THREADMANAGER, "Thread Failed:", Color.RED)

                            if (runningThread.failedException != null) {
                                runningThread.logger.log(
                                    BaseLogger.SECTIONS.THREADMANAGER,
                                    runningThread.failedException.message.toString(),
                                    Color.RED
                                )
                                runningThread.logger.log(
                                    BaseLogger.SECTIONS.THREADMANAGER,
                                    runningThread.failedException.cause?.message.toString(),
                                    Color.RED
                                )
                            }
                        } else {
                            finishedThreads++
                        }
                    }
                }
            }
            sleep(10)
        }

        LOGGER.log(BaseLogger.SECTIONS.THREADMANAGER, "Stopping...", Color.RED)
    }

    fun addThreadToQueue(thread: BaseThread) {
        concurrentThreadQueue.add(thread)
    }
}
