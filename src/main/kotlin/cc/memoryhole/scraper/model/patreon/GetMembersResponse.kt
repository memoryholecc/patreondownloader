package cc.memoryhole.scraper.model.patreon

import com.google.gson.annotations.SerializedName

data class GetMembersResponse(

	@field:SerializedName("data")
	val data: List<DataItem?>? = null,

	@field:SerializedName("meta")
	val meta: Meta? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("included")
	val included: List<IncludedItem?>? = null
)

data class Data(

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("type")
	val type: String? = null
)

data class Attributes(

	@field:SerializedName("youtube")
	val youtube: String? = null,

	@field:SerializedName("thumb_url")
	val thumbUrl: String? = null,

	@field:SerializedName("twitch")
	val twitch: Any? = null,

	@field:SerializedName("gender")
	val gender: Int? = null,

	@field:SerializedName("created")
	val created: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("facebook")
	val facebook: Any? = null,

	@field:SerializedName("about")
	val about: String? = null,

	@field:SerializedName("last_name")
	val lastName: String? = null,

	@field:SerializedName("patron_currency")
	val patronCurrency: String? = null,

	@field:SerializedName("vanity")
	val vanity: String? = null,

	@field:SerializedName("url")
	val url: String? = null,

	@field:SerializedName("twitter")
	val twitter: Any? = null,

	@field:SerializedName("full_name")
	val fullName: String? = null,

	@field:SerializedName("default_country_code")
	val defaultCountryCode: Any? = null,

	@field:SerializedName("social_connections")
	val socialConnections: SocialConnections? = null,

	@field:SerializedName("first_name")
	val firstName: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("avatar_photo_url")
	val avatarPhotoUrl: String? = null
)

data class Twitch(

	@field:SerializedName("url")
	val url: String? = null
)

data class Relationships(

	@field:SerializedName("creator")
	val creator: Creator? = null
)

data class IncludedItem(

	@field:SerializedName("attributes")
	val attributes: Attributes? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("relationships")
	val relationships: Relationships? = null
)

data class Creator(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("links")
	val links: Links? = null
)

data class Twitter(

	@field:SerializedName("url")
	val url: String? = null
)

data class DataItem(

	@field:SerializedName("relationships")
	val relationships: Relationships? = null,

	@field:SerializedName("attributes")
	val attributes: Attributes? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("type")
	val type: String? = null
)

data class Youtube(

	@field:SerializedName("url")
	val url: String? = null
)

data class Meta(

	@field:SerializedName("count")
	val count: Int? = null,

	@field:SerializedName("sort")
	val sort: String? = null
)

data class Instagram(

	@field:SerializedName("url")
	val url: String? = null
)

data class Discord(

	@field:SerializedName("url")
	val url: Any? = null
)

data class Links(

	@field:SerializedName("related")
	val related: String? = null
)

data class SocialConnections(

	@field:SerializedName("youtube")
	val youtube: Any? = null,

	@field:SerializedName("twitter")
	val twitter: Twitter? = null,

	@field:SerializedName("deviantart")
	val deviantart: Any? = null,

	@field:SerializedName("discord")
	val discord: Discord? = null,

	@field:SerializedName("twitch")
	val twitch: Any? = null,

	@field:SerializedName("vimeo")
	val vimeo: Any? = null,

	@field:SerializedName("facebook")
	val facebook: Any? = null,

	@field:SerializedName("spotify")
	val spotify: Any? = null,

	@field:SerializedName("reddit")
	val reddit: Any? = null,

	@field:SerializedName("google")
	val google: Any? = null,

	@field:SerializedName("instagram")
	val instagram: Any? = null
)

data class Campaign(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("links")
	val links: Links? = null
)
