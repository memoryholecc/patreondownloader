package cc.memoryhole.scraper.model.patreon

import cc.memoryhole.scraper.CONFIG
import cc.memoryhole.scraper.config.ScraperConfigSpec
import cc.memoryhole.scraper.graphql.type.PatreonAttachmentDto
import com.google.gson.annotations.SerializedName

data class PatreonMedia(
    @SerializedName("attributes") val mediaAttributes: MediaAttributes,
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String
) {
    fun toPatreonAttachmentDto(post: PatreonPost): PatreonAttachmentDto {
        return PatreonAttachmentDto(
            id = id.toString(),
            displayName = mediaAttributes.fileName ?: "",
            filename = "${CONFIG[ScraperConfigSpec.MinIO.publicEndpoint]}/${post.postRelationships.postCampaign.patreonData.id}/${post.id}/${mediaAttributes.fileName}"
        )
    }
}

data class MediaAttributes(
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("download_url") val downloadUrl: String,
    @SerializedName("file_name") var fileName: String?,
    @SerializedName("image_urls") val mediaImageUrls: MediaImageUrls,
    @SerializedName("metadata") val mediaMetadata: MediaMetadata,
    @SerializedName("mimetype") val mimetype: String,
    @SerializedName("owner_id") val ownerId: String,
    @SerializedName("owner_relationship") val ownerRelationship: String,
    @SerializedName("owner_type") val ownerType: String,
    @SerializedName("size_bytes") val sizeBytes: Long,
    @SerializedName("state") val state: String
)

data class MediaImageUrls(
    @SerializedName("default") val default: String,
    @SerializedName("original") val original: String,
    @SerializedName("thumbnail") val thumbnail: String
)

data class MediaMetadata(
    @SerializedName("dimensions") val mediaDimensions: MediaDimensions
)

data class MediaDimensions(
    @SerializedName("h") val h: Int,
    @SerializedName("w") val w: Int
)