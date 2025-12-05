package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a candidate entity with associated content and metadata.
 *
 * @property content The [io.github.ugaikit.gemini4kt.Content] associated with this candidate,
 * containing the actual content details.
 * @property finishReason A string describing the reason why the processing of this candidate was finished.
 * @property index The index of this candidate in a sequence or batch, indicating its order or position.
 * @property safetyRatings A list of [io.github.ugaikit.gemini4kt.SafetyRating] objects,
 * each representing a safety rating assigned to this candidate's content.
 */
@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String,
    val index: Int? = null,
    val safetyRatings: List<SafetyRating>? = null,
    val citationMetadata: CitationMetadata? = null,
    val tokenCount: Int? = null,
    val avgLogprobs: Double? = null,
    val logprobsResult: LogprobsResult? = null,
    val groundingAttributions: List<GroundingAttribution> = emptyList(),
    val groundingMetadata: GroundingMetadata? = null,
    val urlContextMetadata: UrlContextMetadata? = null,
)

class CandidateBuilder {
    lateinit var content: Content
    lateinit var finishReason: String
    var index: Int = 0
    private var safetyRatings: MutableList<SafetyRating> = mutableListOf()
    var citationMetadata: CitationMetadata? = null
    var tokenCount: Int? = null
    var avgLogprobs: Double? = null
    var logprobsResult: LogprobsResult? = null
    private var groundingAttributions: MutableList<GroundingAttribution> = mutableListOf()
    var groundingMetadata: GroundingMetadata? = null
    var urlContextMetadata: UrlContextMetadata? = null

    fun content(init: ContentBuilder.() -> Unit) {
        content = ContentBuilder().apply(init).build()
    }

    fun safetyRating(init: SafetyRatingBuilder.() -> Unit) {
        val builder = SafetyRatingBuilder().apply(init)
        safetyRatings.add(builder.build())
    }

    fun groundingAttribution(init: GroundingAttributionBuilder.() -> Unit) {
        val builder = GroundingAttributionBuilder().apply(init)
        groundingAttributions.add(builder.build())
    }

    fun build() =
        Candidate(
            content,
            finishReason,
            index,
            if (safetyRatings.isEmpty()) null else safetyRatings,
            citationMetadata,
            tokenCount,
            avgLogprobs,
            logprobsResult,
            groundingAttributions,
            groundingMetadata,
            urlContextMetadata,
        )
}

fun candidate(init: CandidateBuilder.() -> Unit): Candidate = CandidateBuilder().apply(init).build()
