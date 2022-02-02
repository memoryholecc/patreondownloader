package cc.memoryhole.scraper.model.patreon

import com.google.gson.annotations.SerializedName

data class PatreonUser(
    @SerializedName("attributes") val userAttributes: UserAttributes,
    @SerializedName("id") val id: String,
    @SerializedName("relationships") val userRelationships: UserRelationships,
    @SerializedName("type") val type: String
)

data class UserAttributes(
    @SerializedName("about") val about: String,
    @SerializedName("created") val created: String,
    @SerializedName("default_country_code") val defaultCountryCode: String,
    @SerializedName("facebook") val facebook: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("gender") val gender: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("patron_currency") val patronCurrency: String,
    @SerializedName("social_connections") val userSocialConnections: UserSocialConnections,
    @SerializedName("thumb_url") val thumbUrl: String,
    @SerializedName("twitch") val twitch: String,
    @SerializedName("twitter") val twitter: String,
    @SerializedName("url") val url: String,
    @SerializedName("vanity") val vanity: String,
    @SerializedName("youtube") val youtube: String
)

data class UserSocialConnections(
    @SerializedName("deviantart") val deviantart: String,
    @SerializedName("discord") val discord: String,
    @SerializedName("facebook") val facebook: String,
    @SerializedName("google") val google: String,
    @SerializedName("instagram") val instagram: String,
    @SerializedName("reddit") val reddit: String,
    @SerializedName("spotify") val spotify: String,
    @SerializedName("twitch") val twitch: String,
    @SerializedName("twitter") val twitter: String,
    @SerializedName("youtube") val youtube: String
)

data class UserRelationships(
    @SerializedName("campaign") val userCampaign: UserCampaign
)

data class UserCampaign(
    @SerializedName("data") val patreonData: PatreonData,
    @SerializedName("links") val patreonLinks: PatreonLinks
)