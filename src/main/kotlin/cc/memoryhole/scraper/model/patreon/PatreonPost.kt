package cc.memoryhole.scraper.model.patreon

import cc.memoryhole.scraper.CONFIG
import cc.memoryhole.scraper.config.ScraperConfigSpec
import cc.memoryhole.scraper.graphql.type.PatreonEmbedDto
import cc.memoryhole.scraper.graphql.type.PatreonPostDto
import cc.memoryhole.scraper.util.HtmlToPlainTextVisitor
import cc.memoryhole.scraper.util.Utils
import com.apollographql.apollo.api.Input
import com.fasterxml.jackson.databind.JsonNode
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.owasp.html.*
import java.util.ArrayList

data class PatreonPost(
    @SerializedName("attributes") val attributes: PostAttributes,
    @SerializedName("id") val id: String,
    @SerializedName("relationships") val postRelationships: PostRelationships,
    @SerializedName("type") var type: String
) {
    fun toPatreonPostDto(attachments: ArrayList<PatreonMedia>?): PatreonPostDto {
        val title =
            if (attributes.title != null && attributes.title!!.length > 1) {
                attributes.title
            } else {
                ""
            }

        val imageUrl: String? =
            if (attributes.postType != "text_only" && attributes.postImage != null && attributes.postImage.largeUrl.isNotEmpty()) {
                "${CONFIG[ScraperConfigSpec.MinIO.publicEndpoint]}/${postRelationships.postCampaign.patreonData.id}/${id}/cover.png"
            } else {
                null
            }

        val embeds =
            if (attributes.postEmbed != null && attributes.postEmbed.url != null) {
                val subject =
                    if (attributes.postEmbed.subject != null && attributes.postEmbed.subject.isNotEmpty()) {
                        Utils().flattenToAscii(attributes.postEmbed.subject)
                    } else
                        null

                val description =
                    if (attributes.postEmbed.description != null && attributes.postEmbed.description.isNotEmpty()) {
                        Utils().flattenToAscii(attributes.postEmbed.description)
                    } else
                        null
                listOf(
                    PatreonEmbedDto(
                        subject = Input.fromNullable(subject),
                        description = Input.fromNullable(description), //  description = Input.optional(String(attributes.postEmbed.description.toByteArray(), Charsets.US_ASCII)),
                        provider = Input.fromNullable(attributes.postEmbed.provider),
                        url = attributes.postEmbed.url
                    )
                )
            } else {
                null
            }

        val doc: Document = Jsoup.parse(attributes.content)
        val plainContents = HtmlToPlainTextVisitor().getPlainText(doc)

        return PatreonPostDto(
            id = Integer.parseInt(id),
            postedAt = attributes.publishedAt,
            title = title ?: "",
            imageUrl = Input.fromNullable(imageUrl),
            contents = attributes.content,
            plainContents = plainContents,
            embeds = Input.fromNullable(embeds),
            attachments = Input.fromNullable(
                attachments?.map {
                    it.toPatreonAttachmentDto(this)
                }
            )
        )
    }
}

data class PostAttributes(
    @SerializedName("content") var content: String,
    //@JsonIgnoreProperties(ignoreUnknown = true)
    @SerializedName("embed") val postEmbed: PostEmbed,
    @SerializedName("image") val postImage: PostImage,
    @SerializedName("post_type") val postType: String,
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("title") var title: String?,
    @SerializedName("was_posted_by_campaign_owner") val wasPostedByCampaignOwner: Boolean
)

data class PostRelationships(
    @SerializedName("campaign") val postCampaign: PostCampaign,
    @SerializedName("images") val postImages: PostImages,
    @SerializedName("media") val postMedia: PostMedia,
    @SerializedName("poll") val postPoll: PostPoll
)

data class PostEmbed(
    @SerializedName("description") val description: String,
    @SerializedName("html") var html: String,
    @SerializedName("provider") val provider: String,
    @SerializedName("provider_url") val providerUrl: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("url") val url: String
)

data class PostImage(
    @SerializedName("height") val height: Int,
    @SerializedName("large_url") val largeUrl: String,
    @SerializedName("thumb_url") val thumbUrl: String,
    @SerializedName("url") val url: String,
    @SerializedName("width") val width: Int
)

data class PostCampaign(
    @SerializedName("data") val patreonData: PatreonData,
    @SerializedName("links") val patreonLinks: PatreonLinks
)

data class PostImages(
    @SerializedName("data") val data: List<PatreonData>
)

data class PostMedia(
    @SerializedName("data") val data: List<PatreonData>,
)

data class PostPoll(
    @SerializedName("data") val data: PatreonData,
    @SerializedName("links") val patreonLinks: PatreonLinks
)

val contentPolicy: PolicyFactory = HtmlPolicyBuilder()
    //.withPostprocessor { RedirectUrlsSanitizer.AppendDomainAfterText(it) }
    .allowUrlProtocols("http", "https").allowElements("img")
    .allowAttributes("alt", "src", "data-media-id").onElements("img")
    .allowAttributes("border", "height", "width").onElements("img")//.matching(INTEGER)
    .toFactory()
    .and(Sanitizers.FORMATTING) // https://github.com/OWASP/java-html-sanitizer/blob/ca406970b74abd03ec045713b3c9f53963b716ff/src/main/java/org/owasp/html/HtmlPolicyBuilder.java#L260
    .and(Sanitizers.BLOCKS) // https://github.com/OWASP/java-html-sanitizer/blob/ca406970b74abd03ec045713b3c9f53963b716ff/src/main/java/org/owasp/html/HtmlPolicyBuilder.java#L269
    //.and(Sanitizers.IMAGES) // https://github.com/OWASP/java-html-sanitizer/blob/ca406970b74abd03ec045713b3c9f53963b716ff/src/main/java/org/owasp/html/Sanitizers.java#L120
    .and(Sanitizers.LINKS) // https://github.com/OWASP/java-html-sanitizer/blob/ca406970b74abd03ec045713b3c9f53963b716ff/src/main/java/org/owasp/html/Sanitizers.java#L77

val embedPolicy: PolicyFactory = HtmlPolicyBuilder()
    .allowElements("iframe")
    .allowUrlProtocols("https")
    .allowAttributes("src", "class", "width", "height", "frameborder", "scrolling", "allowfullscreen")
    .onElements("iframe")
    .toFactory()


fun fromPostJson(postNode: JsonNode): PatreonPost {
    val post = Gson().fromJson(postNode.toString(), PatreonPost::class.java)

    // Limit max title size to 255 chars
    if (post.attributes.title != null) {
        post.attributes.title = post.attributes.title!!.substring(0, post.attributes.title!!.length.coerceAtMost(255))
    }

    // OWSAP HTML Sanitizer - https://github.com/OWASP/java-html-sanitizer
    post.attributes.content = contentPolicy.sanitize(post.attributes.content)

    if (post.attributes.postEmbed != null)
        post.attributes.postEmbed.html = embedPolicy.sanitize(post.attributes.postEmbed.html)

    return post
}

