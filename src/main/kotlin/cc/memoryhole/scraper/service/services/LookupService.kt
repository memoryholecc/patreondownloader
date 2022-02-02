package cc.memoryhole.scraper.service.services

import cc.memoryhole.scraper.INSTANCE
import cc.memoryhole.scraper.model.patreon.GetMembersResponse
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.InternalServerErrorResponse
import java.util.HashMap

class LookupService {

    val perform: (ctx: Context) -> Unit = { ctx ->
        when (ctx.pathParam("service")) {
            "patreon" -> {
                val sessionId = ctx.formParamAsClass<String>("sessionId").check({ it.length == 43 }, "INVALID_SESSIONID").get()

                val returningPledges = HashMap<Int, String>()

                //https://www.patreon.com/api/pledges?include=campaign&fields[campaign]=avatar_photo_url,cover_photo_url,is_monthly,is_non_profit,name,pay_per_name,pledge_url,published_at,url&fields[user]=thumb_url,url,full_name&fields[pledge]=amount_cents,currency,pledge_cap_cents,cadence,created_at,has_shipping_address,is_paused,status&fields[reward]=description,requires_shipping,unpublished_at&fields[reward-item]=id,title,description,requires_shipping,item_type,is_published,is_ended,ended_at,reward_item_configuration&json-api-use-default-includes=false&json-api-version=1.0

                //https://www.patreon.com/api/pledges?include=campaign&fields[campaign]=avatar_photo_url,cover_photo_url,is_monthly,is_non_profit,name,pay_per_name,pledge_url,published_at,url&fields[user]=thumb_url,url,full_name&fields[pledge]=amount_cents,currency,pledge_cap_cents,cadence,created_at,has_shipping_address,is_paused,status&fields[reward]=description,requires_shipping,unpublished_at&fields[reward-item]=id,title,description,requires_shipping,item_type,is_published,is_ended,ended_at,reward_item_configuration&json-api-use-default-includes=false&json-api-version=1.0
                val URL =
                    "https://www.patreon.com/api/pledges?include=campaign&fields[campaign]=name&json-api-use-default-includes=false&json-api-version=1.0"

                val response = INSTANCE.webApi.getWithSessionCookie(URL, sessionId)
                    ?: throw InternalServerErrorResponse("Error requesting pledges: response is null")

                if (!response.isSuccessful) throw InternalServerErrorResponse("Error requesting pledges: response is not successful (${response.code})")

                val responseBody = response.body?.string()
                    ?: throw InternalServerErrorResponse("Error requesting pledges: response body is empty")

                try {
                    val mapper = ObjectMapper()
                    val rootNode = mapper.readTree(responseBody)
                    val includedNode = rootNode["included"]
                        ?: throw InternalServerErrorResponse("Error requesting pledges: response does not contain results (${response.code}, ${responseBody})")

                    for (pledgeNode in includedNode) {
                        if (pledgeNode["type"].asText() == "campaign") {
                            val id = pledgeNode["id"].asInt()
                            returningPledges[id] = pledgeNode["attributes"]["name"].asText()
                        }
                    }
                } catch (e: JsonProcessingException) {
                    e.printStackTrace()
                }
                ctx.json(returningPledges)
            }
            else -> {
                throw BadRequestResponse("Invalid service")
            }
        }
    }


    private fun getCurrentUserId(sessionId: String): String? {
        val url =
            "https://www.patreon.com/api/current_user?include=campaign.null&fields[user]=full_name%2Cimage_url&fields[campaign]=name%2Cavatar_photo_url&json-api-version=1.0"
        val response = INSTANCE.webApi.getWithSessionCookie(url, sessionId)
            ?: throw InternalServerErrorResponse("Error requesting pledges: response is null")

        if (!response.isSuccessful) throw InternalServerErrorResponse("Error requesting pledges: response is not successful (${response.code})")

        val responseBody = response.body?.string()
            ?: throw InternalServerErrorResponse("Error requesting pledges: response body is empty")
        try {
            val mapper = ObjectMapper()
            val rootNode = mapper.readTree(responseBody)
            return rootNode["data"]["id"].asText()
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getMembers(userId: String?, sessionId: String): HashMap<Int, String> {
        val members = HashMap<Int, String>()

        val url =
            "https://www.patreon.com/api/members?filter[user_id]=$userId&filter[can_be_messaged]=true&include=campaign.creator.null&fields[member]=[]&fields[campaign]=avatar_photo_url%2Cname%2Cid%2Curl&page[count]=500&json-api-use-default-includes=false&json-api-version=1.0"
        val response = INSTANCE.webApi.getWithSessionCookie(url, sessionId)
            ?: throw InternalServerErrorResponse("Error requesting pledges: response is null")

        if (!response.isSuccessful) throw InternalServerErrorResponse("Error requesting pledges: response is not successful (${response.code})")

        val responseBody = response.body?.string()
            ?: throw InternalServerErrorResponse("Error requesting pledges: response body is empty")

        val getMembersResponse = Gson().fromJson(responseBody, GetMembersResponse::class.java)

        /*try {
            val mapper = ObjectMapper()
            val rootNode = mapper.readTree(responseBody)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }*/

        getMembersResponse.included?.forEach {
            if (it != null) {
                if (it.type == "campaign") {
                    val id = it.id
                    val name = it.attributes?.name
                    if (name != null) {
                        members[Integer.parseInt(id)] = name
                    }
                }
            }
        }

        return members
    }
}