package cc.memoryhole.scraper.model.patreon

import com.google.gson.annotations.SerializedName

data class PatreonLinks(
    @SerializedName("related") val related: String
)
