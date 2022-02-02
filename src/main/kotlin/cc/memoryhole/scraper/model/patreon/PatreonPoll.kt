package cc.memoryhole.scraper.model.patreon

import com.google.gson.annotations.SerializedName

data class PatreonPoll(
    @SerializedName("relationships") val relationships: PollRelationships,
    @SerializedName("attributes") val attributes: PollAttributes,
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String
)

data class PollRelationships(
    @SerializedName("choices") val choices: Choices
)

data class PollAttributes(
    @SerializedName("num_responses") val numResponses: Int,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("question_type") val questionType: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("closes_at") val closesAt: Any
)

data class Choices(
    @SerializedName("data") val data: List<PatreonData>
)
