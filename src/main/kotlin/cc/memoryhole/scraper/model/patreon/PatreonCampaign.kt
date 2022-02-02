package cc.memoryhole.scraper.model.patreon

import com.google.gson.annotations.SerializedName

data class PatreonCampaign(
    @SerializedName("attributes") val campaignAttributes: CampaignAttributes,
    @SerializedName("id") val id: Int,
    @SerializedName("relationships") val campaignRelationships: CampaignRelationships,
    @SerializedName("type") val type: String
)

data class CampaignAttributes(
    @SerializedName("avatar_photo_url") val avatarPhotoUrl: String,
    @SerializedName("campaign_pledge_sum") val campaignPledgeSum: Int,
    @SerializedName("cover_photo_url") val coverPhotoUrl: String,
    @SerializedName("cover_photo_url_sizes") val campaignCoverPhotoUrlSizes: CampaignCoverPhotoUrlSizes,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("creation_count") val creationCount: Int,
    @SerializedName("creation_name") val creationName: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("discord_server_id") val discordServerId: Long,
    @SerializedName("display_patron_goals") val displayPatronGoals: Boolean,
    @SerializedName("earnings_visibility") val earningsVisibility: String,
    @SerializedName("image_small_url") val imageSmallUrl: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("is_charge_upfront") val isChargeUpfront: Boolean,
    @SerializedName("is_charged_immediately") val isChargedImmediately: Boolean,
    @SerializedName("is_monthly") val isMonthly: Boolean,
    @SerializedName("is_nsfw") val isNsfw: Boolean,
    @SerializedName("is_plural") val isPlural: Boolean,
    @SerializedName("main_video_embed") val mainVideoEmbed: String,
    @SerializedName("main_video_url") val mainVideoUrl: String,
    @SerializedName("name") val name: String,
    @SerializedName("one_liner") val oneLiner: String,
    @SerializedName("outstanding_payment_amount_cents") val outstandingPaymentAmountCents: Int,
    @SerializedName("patron_count") val patronCount: Int,
    @SerializedName("pay_per_name") val payPerName: String,
    @SerializedName("pledge_sum") val pledgeSum: Int,
    @SerializedName("pledge_sum_currency") val pledgeSumCurrency: String,
    @SerializedName("pledge_url") val pledgeUrl: String,
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("thanks_embed") val thanksEmbed: String,
    @SerializedName("thanks_msg") val thanksMsg: String,
    @SerializedName("thanks_video_url") val thanksVideoUrl: String,
    @SerializedName("url") val url: String
)

data class CampaignCoverPhotoUrlSizes(
    @SerializedName("large") val large: String,
    @SerializedName("medium") val medium: String,
    @SerializedName("small") val small: String
)

data class CampaignRelationships(
    @SerializedName("creator") val campaignCreator: CampaignCreator
)

data class CampaignCreator(
    @SerializedName("data") val campaignData: PatreonData,
    @SerializedName("links") val campaignLinks: PatreonLinks
)
