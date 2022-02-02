package cc.memoryhole.scraper.thread.impl.patreon

import cc.memoryhole.scraper.CONFIG
import cc.memoryhole.scraper.INSTANCE
import cc.memoryhole.scraper.config.ScraperConfigSpec
import cc.memoryhole.scraper.graphql.*
import cc.memoryhole.scraper.graphql.type.CreatorDto
import cc.memoryhole.scraper.model.patreon.*
import cc.memoryhole.scraper.thread.BaseThread
import cc.memoryhole.scraper.util.Utils
import cc.memoryhole.scraper.util.WebAPI
import cc.memoryhole.scraper.util.logging.BaseLogger
import cc.memoryhole.scraper.util.logging.ImportLogger
import com.andreapivetta.kolor.Color
import com.apollographql.apollo.api.Input
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class PatreonCampaignScrapeThread(
    private var importId: UUID,
    private var campaignId: String,
    private var sessionId: String
) : BaseThread() {

    private val webAPI = WebAPI()
    private val utils = Utils()

    /**
     * List of all posts
     */
    private val posts: ArrayList<PatreonPost> = ArrayList()

    /**
     * Map of all comments for each post(id)
     */
    private val postComments: HashMap<String, ArrayList<PatreonComment>> = HashMap()

    /**
     * Map of all commenters
     */
    // commenterId, commenter
    private val postCommenters: HashMap<String, PatreonUser> = HashMap()

    /**
     * Map of all media for posts
     */
    private val postMedia: HashMap<String, ArrayList<PatreonMedia>> = HashMap()

    /**
     * Map of all polls for posts
     */
    // pollId, <pollChoiceId, <choice, votes>>
    private val postPoll: HashMap<String, HashMap<String, Pair<String, Int>?>> = HashMap()

    private lateinit var campaign: PatreonCampaign

    init {
        priority = 10
    }

    override fun run() {
        logger = ImportLogger(importId, campaignId)

        logger.log(BaseLogger.SECTIONS.GENERAL, "Starting Import")
        logger.log(BaseLogger.SECTIONS.PATREON, "Downloading First Page")
        val url =
            "https://www.patreon.com/api/campaigns/${campaignId}/posts" +
                    "?include=comments,user,creator,campaign,images,media,poll,poll.choices" + // campaign,access_rules,attachments,audio,images,media,poll.choices,poll.current_user_responses.user,poll.current_user_responses.choice,poll.current_user_responses.poll,user,user_defined_tags,ti_checks
                    "&fields[post]=content,embed,image,published_at,post_type,thumbnail_url,title,was_posted_by_campaign_owner" +
                    "&filter[contains_exclusive_posts]=true" +
                    "&filter[is_draft]=false" +
                    "&filter[is_community]=false" +
                    "&filter[post.was_posted_by_campaign_owner]=true" +
                    "&sort=-published_at" +
                    "&page[count]=999999"

        try {
            importCampaignStep(url)
        } catch (ex: Exception) {
            failedException = ex
            failed = true
            logger.error(BaseLogger.SECTIONS.SCRAPER, ex)
            if (CONFIG[ScraperConfigSpec.env] == "dev")
                ex.printStackTrace()
        }

        if (!CONFIG[ScraperConfigSpec.Debugging.skipComments]) {
            for (post in posts) {
                logger.log(BaseLogger.SECTIONS.PATREON, "Downloading First Comments Page for Post ${post.id}")
                val url = "https://www.patreon.com/api/posts/${post.id}/comments" +
                        "?include=parent,post,on_behalf_of_campaign.null,commenter.campaign.null,commenter.flairs.campaign,first_reply.commenter.campaign.null,first_reply.parent,first_reply.post,first_reply.on_behalf_of_campaign.null" +
                        "&fields[campaign]=[]" +
                        "&fields[comment]=body,created,deleted_at,is_by_patron,is_by_creator,vote_sum,current_user_vote,reply_count&fields[flair]=image_tiny_url,name" +
                        "&fields[post]=comment_count" +
                        "&fields[post_tag]=tag_type,value" +
                        "&fields[user]=image_url,full_name,url" +
                        "&page[count]=999" +
                        "&sort=-created" +
                        "&json-api-version=1.0"
                try {
                    importCommentsStep(url)
                } catch (ex: Exception) {
                    failedException = ex
                    failed = true
                    logger.error(BaseLogger.SECTIONS.SCRAPER, ex)
                    if (CONFIG[ScraperConfigSpec.env] == "dev")
                        ex.printStackTrace()
                }
            }
        }

        if (postPoll.isNotEmpty()) {
            logger.log(BaseLogger.SECTIONS.GENERAL, "Sorting poll choices by id")

            for (poll in postPoll.values)
                poll.toSortedMap(compareByDescending { it })
        }

        logger.log(BaseLogger.SECTIONS.GENERAL, "Replacing all patreon links by internal ones")
        replaceInlineImages()

        logger.log(BaseLogger.SECTIONS.GENERAL, "Adding posts to download queue")

        for (post in posts.sortedBy { it.id }) {
            INSTANCE.threadManager.addThreadToQueue(
                PatreonPostDownloadThread(
                    importId,
                    post
                )
            )
        }

        if (!CONFIG[ScraperConfigSpec.Debugging.skipMedia]) {
            logger.log(BaseLogger.SECTIONS.GENERAL, "Adding media to download queue")

            for (media in postMedia.values.sortedBy { it[0].id }) {
                for (patreonMedia in media.sortedBy { it.id }) {
                    INSTANCE.threadManager.addThreadToQueue(
                        PatreonMediaDownloadThread(
                            importId,
                            campaignId,
                            patreonMedia
                        )
                    )
                }
            }
        } else {
            logger.log(BaseLogger.SECTIONS.GENERAL, "Skipping media", Color.RED)
        }

        logger.log(BaseLogger.SECTIONS.GENERAL, "Adding campaign to download queue")

        if (this::campaign.isInitialized) {
            INSTANCE.threadManager.addThreadToQueue(
                PatreonCampaignDownloadThread(
                    importId,
                    campaign
                )
            )
        }

        /**
         * Send to Backend
         */

        logger.log(BaseLogger.SECTIONS.BACKEND, "Sending to backend")

        sendToBackend()
    }


    private fun importCampaignStep(url: String) {
        val cleanedUrl = if (url.startsWith("www.")) ("https://$url") else (url)

        val response: Response? = webAPI.getWithSessionCookie(cleanedUrl, sessionId)
        /**
         * data: posts
         * included: comments, user, creator, media, campaign
         * links: pagination links
         * meta: post count
         */
        if (response != null) {
            if (response.code == 200) {
                val jsonContent = response.body?.string()

                try {
                    val objectMapper = ObjectMapper()

                    val rootNode: JsonNode = objectMapper.readTree(jsonContent)

                    if (rootNode["errors"] !is NullNode) {

                        val campaignDataNode = rootNode["data"]
                        for (jsonNode in campaignDataNode) {
                            val post = parsePostFromNode(jsonNode)
                            if (post.attributes.wasPostedByCampaignOwner)
                                posts.add(post)
                        }

                        // INCLUDED DATA
                        val includedDataNode = rootNode["included"]
                        for (includedDataEntry in includedDataNode) {
                            when (includedDataEntry["type"].asText()) {
                                //TODO: Other stuff maybe idk
                                "media" -> {
                                    val media = Gson().fromJson(includedDataEntry.toString(), PatreonMedia::class.java)

                                    media.mediaAttributes.fileName = utils.guessPatreonMediaFileName(media)

                                    if (postMedia[media.mediaAttributes.ownerId] == null)
                                        postMedia[media.mediaAttributes.ownerId] = ArrayList()

                                    postMedia[media.mediaAttributes.ownerId]?.add(media)
                                }
                                "campaign" -> {
                                    val campaign =
                                        Gson().fromJson(includedDataEntry.toString(), PatreonCampaign::class.java)
                                    if (campaign.id.toString() == campaignId) {
                                        this.campaign = campaign
                                    }
                                }
                                "user" -> {

                                }
                                "poll" -> {
                                    val poll =
                                        Gson().fromJson(includedDataEntry.toString(), PatreonPoll::class.java)

                                    val pollChoicesMap = hashMapOf<String, Pair<String, Int>?>()

                                    for (choice in poll.relationships.choices.data) {
                                        pollChoicesMap[choice.id] = null
                                    }

                                    postPoll[poll.id] = pollChoicesMap
                                }
                                "poll_choice" -> {
                                    val pollChoice =
                                        Gson().fromJson(includedDataEntry.toString(), PatreonPollChoice::class.java)

                                    for (poll in postPoll) {
                                        if (poll.value.containsKey(pollChoice.id)) {
                                            poll.value[pollChoice.id] = Pair(
                                                pollChoice.attributes.textContent,
                                                pollChoice.attributes.numResponses
                                            )
                                        }
                                    }
                                }
                                "comment" -> {
/*                                    val comment =
                                        Gson().fromJson(includedDataEntry.toString(), PatreonComment::class.java)

                                    val postId = comment.patreonCommentRelationship.patreonCommentPost.patreonData.id
                                    if (postComments[postId] == null)
                                        postComments[postId] = ArrayList()

                                    postComments[postId]?.add(comment)*/
                                }
                                "reward" -> {

                                }
                                "goal" -> {

                                }
                                else -> {
                                    println(this)
                                }
                                /*"user" -> {
                                    postMedia[campaignId]?.add(
                                        Pair(
                                            includedDataEntry["type"].asText(),
                                            Gson().fromJson(includedDataEntry.toString(), PatreonUser::class.java)
                                        )
                                    )
                                }
                                "reward" -> {
                                    postMedia[campaignId]?.add(
                                        Pair(
                                            includedDataEntry["type"].asText(),
                                            Gson().fromJson(includedDataEntry.toString(), PatreonReward::class.java)
                                        )
                                    )
                                }*/
                            }
                        }
                        if (rootNode["links"]["next"] != null) {
                            logger.log(BaseLogger.SECTIONS.PATREON, "Downloading Next Page")
                            importCampaignStep(rootNode["links"]["next"].asText())
                        }
                    }
                } catch (e: JsonProcessingException) {
                    e.printStackTrace()
                }
            } else {
                logger.log(BaseLogger.SECTIONS.PATREON, "Error downloading data")
                println(response.code)
                println(response.request.url)
            }
        }
        response?.close()
    }

    private fun importCommentsStep(url: String) {
        val cleanedUrl = if (url.startsWith("www.")) ("https://$url") else (url)

        val response: Response? = webAPI.getWithSessionCookie(cleanedUrl, sessionId)

        if (response != null) {
            if (response.code == 200) {
                val jsonContent = response.body?.string()

                try {
                    val objectMapper = ObjectMapper()

                    val rootNode: JsonNode = objectMapper.readTree(jsonContent)

                    if (rootNode["errors"] !is NullNode) {

                        val campaignDataNode = rootNode["data"]
                        for (jsonNode in campaignDataNode) {
                            val comment = Gson().fromJson(jsonNode.toString(), PatreonComment::class.java)
                            val postId = comment.patreonCommentRelationship.patreonCommentPost.patreonData.id
                            if (postComments[postId] == null)
                                postComments[postId] = ArrayList()
                            postComments[postId]?.add(comment)
                        }

                        // INCLUDED DATA
                        val includedDataNode = rootNode["included"]
                        if (includedDataNode != null)
                            for (includedDataEntry in includedDataNode) {
                                when (includedDataEntry["type"].asText()) {
                                    "comment" -> {
                                        //      val comment =
                                        //         Gson().fromJson(includedDataEntry.toString(), PatreonComment::class.java)
                                    }
                                    "user" -> {
                                        val user =
                                            Gson()
                                                .fromJson(includedDataEntry.toString(), PatreonUser::class.java)
                                        postCommenters[user.id] = user
                                    }
                                    "goal" -> {

                                    }
                                    else -> {
                                        //    println(includedDataEntry)
                                    }
                                }
                            }

                        if (rootNode["links"]["next"] != null) {
                            logger.log(BaseLogger.SECTIONS.PATREON, "Downloading Next Comments Page")
                            importCommentsStep(rootNode["links"]["next"].asText())
                        }
                    }
                } catch (e: JsonProcessingException) {
                    e.printStackTrace()
                }
            } else {
                logger.log(BaseLogger.SECTIONS.PATREON, "Error downloading data")
                println(response.code)
                println(response.request.url)
            }
        }
        response?.close()
    }

    private fun parsePostFromNode(postNode: JsonNode): PatreonPost {
        return fromPostJson(postNode)
    }


    private fun replaceInlineImages() {
        // scan all posts for data-media-id & search for attachments & set the filename
        for (post in posts) {
            val doc: Document = Jsoup.parse(post.attributes.content)
            val images: Elements = doc.getElementsByTag("img")

            for (image in images) {
                val imageMediaId = image.attributes().get("data-media-id")

                var associated = false
                for (media in postMedia) {
                    for (patreonMedia in media.value) {
                        if (patreonMedia.id.toString() == imageMediaId) {

                            associated = true

                            image.attributes().put(
                                "src",
                                "${CONFIG[ScraperConfigSpec.MinIO.publicEndpoint]}/${post.postRelationships.postCampaign.patreonData.id}/${post.id}/${patreonMedia.mediaAttributes.fileName}"
                            )
                            image.attributes().put(
                                "alt",
                                "This image is still being downloaded. Please be patient."
                            )
                            image.attributes().put(
                                "style",
                                "max-width: 100%; max-height: 40vh"
                            )
                        }
                    }
                }
                if (!associated)
                    logger.error(
                        BaseLogger.SECTIONS.SCRAPER,
                        "Could not associate image (ID: $imageMediaId - ${
                            image.attributes().get("src")
                        }) to downloaded media"
                    )
            }
            post.attributes.content = doc.body().html()
        }
    }

    private fun sendToBackend() {
        runBlocking {
            logger.log(BaseLogger.SECTIONS.BACKEND, "Checking for existing creator")
            val patreonCreator = INSTANCE.apolloQuery(GetPatreonByCampaignIdQuery(id = campaignId), true)

            lateinit var creatorId: String

            if (patreonCreator != null && !patreonCreator.hasErrors()) {
                logger.log(BaseLogger.SECTIONS.BACKEND, "Creator exists")

                // creatorId = patreonCreator?.data?.getPatreonByCampaignId?.creator?.id.toString()
            } else {
                logger.log(BaseLogger.SECTIONS.BACKEND, "Creator doesn't exist, creating new one", Color.RED)
                val avatarFileName = utils.guessNameFromURL(campaign.campaignAttributes.avatarPhotoUrl)
                val coverFileName = utils.guessNameFromURL(campaign.campaignAttributes.coverPhotoUrl)

                val profilePicture = "${CONFIG[ScraperConfigSpec.MinIO.publicEndpoint]}/$campaignId/avatar.${avatarFileName.split(".").last()}"
                val bannerPicture = "${CONFIG[ScraperConfigSpec.MinIO.publicEndpoint]}/$campaignId/cover.${coverFileName.split(".").last()}"

                val createCreatorMutationResponse = INSTANCE.apolloMutation(
                    CreateCreatorMutation(
                        CreatorDto(
                            name = campaign.campaignAttributes.name,
                            profilePicture = Input.optional(profilePicture),
                            bannerPicture = Input.optional(bannerPicture)
                        )
                    )
                )

                logger.log(BaseLogger.SECTIONS.BACKEND, "Checking for response data")
                if (createCreatorMutationResponse?.data != null) {
                    logger.log(BaseLogger.SECTIONS.BACKEND, "Response data not null, creating new PatreonCreator")
                    creatorId = createCreatorMutationResponse.data!!.createCreator.id

                    INSTANCE.apolloMutation(
                        CreatePatreonMutation(
                            campaignId = campaignId,
                            id = creatorId,
                            username = campaign.campaignAttributes.name
                        )
                    )
                }
            }

            val chunkSize = 250

            logger.log(BaseLogger.SECTIONS.BACKEND, "Sending posts to backend in chunks of $chunkSize")

            val chunked = posts.chunked(chunkSize)
            for (postsChunk in chunked) {
                logger.log(BaseLogger.SECTIONS.BACKEND, "Sending posts chunk ${chunked.indexOf(postsChunk) + 1}")
                INSTANCE.apolloMutation(
                    AddPatreonPostsMutation(
                        campaignId = campaignId,
                        posts = postsChunk.map {
                            it.toPatreonPostDto(postMedia[it.id])
                        }
                    )
                )
            }

            logger.log(BaseLogger.SECTIONS.BACKEND, "Sent all posts to backend :)")

            logger.log(BaseLogger.SECTIONS.BACKEND, "Sending comments")

            for (post in posts) {
                INSTANCE.apolloMutation(
                    AddPatreonPostCommentsMutation(
                        postId = post.id,
                        comments = postComments[post.id]?.map {
                            it.toPatreonCommentDto(postCommenters[it.patreonCommentRelationship.patreonCommentCommenter.patreonCommentData.id])
                        } ?: ArrayList(),
                    )
                )
            }
            logger.log(BaseLogger.SECTIONS.BACKEND, "Sent all comments to backend :)")

            logger.log(BaseLogger.SECTIONS.BACKEND, "Finished Scraping ${posts.count()} Posts, ${postComments.count()} Comments and ${postMedia.count()} Attachments.")

            clearData()
        }
    }

    private fun clearData() {
        posts.clear()
        postComments.clear()
        postCommenters.clear()
        postMedia.clear()
        postPoll.clear()
    }
}
