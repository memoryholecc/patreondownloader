package cc.memoryhole.scraper.service.services

import cc.memoryhole.scraper.INSTANCE
import cc.memoryhole.scraper.thread.impl.patreon.PatreonCampaignScrapeThread
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import java.util.*

class EnqueueService {
    val perform: (ctx: Context) -> Unit = { ctx ->
        when (ctx.pathParam("service")) {
            "patreon" -> {
                val sessionId =
                    ctx.formParamAsClass<String>("sessionId")
                        .check({ it.length == 43 }, "INVALID_SESSIONID")
                        .get()

                val idList =
                    ctx.formParamAsClass<String>("toImport")
                        .check({ it.toIntOrNull() != null || it.contains(',') }, "INVALID_IDS")
                        .get()

                val importId = UUID.randomUUID()
                if (idList.toIntOrNull() != null) {
                    INSTANCE.threadManager.addThreadToQueue(
                        PatreonCampaignScrapeThread(
                            importId,
                            idList,
                            sessionId
                        )
                    )
                } else {
                    for (id in idList.split(",")) {
                        if (id.toIntOrNull() != null) {
                            INSTANCE.threadManager.addThreadToQueue(
                                PatreonCampaignScrapeThread(
                                    importId,
                                    id,
                                    sessionId
                                )
                            )
                        }
                    }
                }
                ctx.json(hashMapOf("importId" to importId))
            }
            else -> {
                throw BadRequestResponse("Invalid service")
            }
        }
    }
}
