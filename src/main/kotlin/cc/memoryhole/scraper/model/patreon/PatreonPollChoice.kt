package cc.memoryhole.scraper.model.patreon

import com.google.gson.annotations.SerializedName

data class PatreonPollChoice(
    @SerializedName("attributes") val attributes: PollChoiceAttributes,
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String
)

data class PollChoiceAttributes(
    @SerializedName("num_responses") val numResponses: Int,
    @SerializedName("text_content") val textContent: String,
    @SerializedName("choice_type") val choiceType: String,
    @SerializedName("position") val position: Int
)
