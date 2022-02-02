package cc.memoryhole.scraper.model.patreon

import com.google.gson.annotations.SerializedName

data class PatreonData(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String
)