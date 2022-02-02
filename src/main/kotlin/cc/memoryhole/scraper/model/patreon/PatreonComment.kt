package cc.memoryhole.scraper.model.patreon

import cc.memoryhole.scraper.graphql.type.PatreonCommentDto
import com.apollographql.apollo.api.Input
import com.google.gson.annotations.SerializedName

data class PatreonComment(
    @SerializedName("attributes") val patreonCommentAttributes: PatreonCommentAttributes,
    @SerializedName("id") val id: Int,
    @SerializedName("relationships") val patreonCommentRelationship: PatreonCommentRelationship,
    @SerializedName("type") val type: String
) {
    fun toPatreonCommentDto(user: PatreonUser?): PatreonCommentDto {
        return PatreonCommentDto(
            id = id.toString(),
            authorUsername = user?.userAttributes?.fullName
                ?: patreonCommentRelationship.patreonCommentCommenter.patreonCommentData.id,
            authorPicture = Input.optional(user?.userAttributes?.imageUrl ?: ""),
        authorUrl = Input.optional(user?.userAttributes?.url ?: ""),
        postedAt = patreonCommentAttributes.created,
        contents = patreonCommentAttributes.body
        )
    }
}

data class PatreonCommentAttributes(
    @SerializedName("body") val body: String,
    @SerializedName("created") val created: String,
    @SerializedName("current_user_vote") val currentUserVote: Int,
    @SerializedName("deleted_at") val deletedAt: String,
    @SerializedName("is_by_creator") val isByCreator: Boolean,
    @SerializedName("is_by_patron") val isByPatron: Boolean,
    @SerializedName("reply_count") val replyCount: Int,
    @SerializedName("vote_sum") val voteSum: Int
)

data class PatreonCommentRelationship(
    @SerializedName("commenter") val patreonCommentCommenter: PatreonCommentCommenter,
    @SerializedName("first_reply") val patreonCommentFirstReply: PatreonCommentFirstReply,
    //@SerializedName("on_behalf_of_campaign") val patreonCommentOnBehalfOfCampaign: PatreonCommentOnBehalfOfCampaign,
    @SerializedName("parent") val patreonCommentParent: PatreonCommentParent,
    @SerializedName("post") val patreonCommentPost: PatreonCommentPost
)

data class PatreonCommentCommenter(
    @SerializedName("data") val patreonCommentData: PatreonData,
    @SerializedName("links") val patreonCommentLinks: PatreonLinks
)

data class PatreonCommentFirstReply(
    @SerializedName("data") val data: PatreonData,
    @SerializedName("links") val links: PatreonLinks
)

data class PatreonCommentOnBehalfOfCampaign(
    @SerializedName("data") val data: String
)

data class PatreonCommentParent(
    @SerializedName("data") val data: String
)

data class PatreonCommentPost(
    @SerializedName("data") val patreonData: PatreonData,
    @SerializedName("links") val patreonLinks: PatreonLinks
)