package cc.memoryhole.scraper.model.patreon

import com.google.gson.annotations.SerializedName

data class PatreonReward(
    @SerializedName("attributes") val rewardAttributes: RewardAttributes,
    @SerializedName("id") val id: Int,
    @SerializedName("relationships") val rewardRelationships: RewardRelationships,
    @SerializedName("type") val type: String
)

data class RewardAttributes(
    @SerializedName("amount") val amount: Int,
    @SerializedName("amount_cents") val amountCents: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("description") val description: String,
    @SerializedName("discord_role_ids") val discordRoleIds: List<Int>,
    @SerializedName("edited_at") val editedAt: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("patron_amount_cents") val patronAmountCents: Int,
    @SerializedName("patron_count") val patronCount: Int,
    @SerializedName("patron_currency") val patronCurrency: String,
    @SerializedName("post_count") val postCount: Int,
    @SerializedName("published") val published: Boolean,
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("remaining") val remaining: Int,
    @SerializedName("requires_shipping") val requiresShipping: Boolean,
    @SerializedName("title") val title: String,
    @SerializedName("unpublished_at") val unpublishedAt: String,
    @SerializedName("url") val url: String,
    @SerializedName("user_limit") val userLimit: Int
)

data class RewardRelationships(
    @SerializedName("campaign") val rewardCampaign: RewardCampaign
)

data class RewardCampaign(
    @SerializedName("data") val patreonData: PatreonData,
    @SerializedName("links") val patreonLinks: PatreonLinks
)
