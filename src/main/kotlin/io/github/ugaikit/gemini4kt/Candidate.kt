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
    val safetyRatings: List<SafetyRating>,
    val citationMetadata: CitationMetadata? = null,
    val tokenCount: Int? = null,
    val avgLogprobs: Double? = null,
    val logprobsResult: LogprobsResult? = null,
    val groundingAttributions: List<GroundingAttribution> = emptyList(),
)
