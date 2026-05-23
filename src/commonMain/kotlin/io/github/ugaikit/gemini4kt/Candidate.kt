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
 * @property citationMetadata Citation sources details for the content.
 * @property tokenCount Token count of the candidate's generated content.
 * @property avgLogprobs Average log probability of the candidate.
 * @property logprobsResult Log probability details per token.
 * @property groundingAttributions List of grounding attributions.
 * @property groundingMetadata Grounding metadata returned by tools like Search.
 * @property urlContextMetadata Metadata of extracted URL contexts.
 * @property finishMessage An optional message explaining the finish reason.
 */
@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String? = null,
    val index: Int? = null,
    val safetyRatings: List<SafetyRating>? = null,
    val citationMetadata: CitationMetadata? = null,
    val tokenCount: Int? = null,
    val avgLogprobs: Double? = null,
    val logprobsResult: LogprobsResult? = null,
    val groundingAttributions: List<GroundingAttribution> = emptyList(),
    val groundingMetadata: GroundingMetadata? = null,
    val urlContextMetadata: UrlContextMetadata? = null,
    val finishMessage: String? = null,
)

/**
 * Represents the candidate builder.
 */
class CandidateBuilder {
    /**
     * Holds the content.
     */
    lateinit var content: Content

    /**
     * Holds the finish reason.
     */
    var finishReason: String? = null

    /**
     * Holds the index.
     */
    var index: Int = 0

    /**
     * Holds the safety ratings.
     */
    private var safetyRatings: MutableList<SafetyRating> = mutableListOf()

    /**
     * Holds the citation metadata.
     */
    var citationMetadata: CitationMetadata? = null

    /**
     * Holds the token count.
     */
    var tokenCount: Int? = null

    /**
     * Holds the avg logprobs.
     */
    var avgLogprobs: Double? = null

    /**
     * Holds the logprobs result.
     */
    var logprobsResult: LogprobsResult? = null

    /**
     * Holds the grounding attributions.
     */
    private var groundingAttributions: MutableList<GroundingAttribution> = mutableListOf()

    /**
     * Holds the grounding metadata.
     */
    var groundingMetadata: GroundingMetadata? = null

    /**
     * Holds the url context metadata.
     */
    var urlContextMetadata: UrlContextMetadata? = null

    /**
     * Holds the finish message.
     */
    var finishMessage: String? = null

    /**
     * Handles content.
     *
     * @param init The init.
     */
    fun content(init: ContentBuilder.() -> Unit) {
        content = ContentBuilder().apply(init).build()
    }

    /**
     * Handles safety rating.
     *
     * @param init The init.
     */
    fun safetyRating(init: SafetyRatingBuilder.() -> Unit) {
        val builder = SafetyRatingBuilder().apply(init)
        safetyRatings.add(builder.build())
    }

    /**
     * Handles grounding attribution.
     *
     * @param init The init.
     */
    fun groundingAttribution(init: GroundingAttributionBuilder.() -> Unit) {
        val builder = GroundingAttributionBuilder().apply(init)
        groundingAttributions.add(builder.build())
    }

    /**
     * Handles build.
     */
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
            finishMessage,
        )
}

/**
 * Handles candidate.
 *
 * @param init The init.
 */
fun candidate(init: CandidateBuilder.() -> Unit): Candidate = CandidateBuilder().apply(init).build()
