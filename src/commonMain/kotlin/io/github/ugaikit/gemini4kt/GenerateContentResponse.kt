package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the response structure for a content generation or analysis request.
 *
 * This data class encapsulates the results of a request to generate or analyze content,
 * including a list of generated content candidates and feedback on the prompt used for generation.
 * The feedback includes safety ratings that assess the prompt's compliance with various content guidelines.
 *
 * @property candidates A list of [Candidate] objects, each representing a possible outcome or version
 *                      of the generated or analyzed content.
 * @property promptFeedback A [PromptFeedback] object providing safety ratings for the prompt,
 *                          offering insights into its adherence to content safety standards.
 * @property usageMetadata Metadata concerning token usage and stats for this request.
 * @property modelVersion The version of the model used to generate the content.
 * @property responseId The unique ID of the generation response.
 * @property modelStatus The status/error details of the model execution.
 */
@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList(),
    val promptFeedback: PromptFeedback? = null,
    val usageMetadata: UsageMetadata? = null,
    val modelVersion: String = "",
    val responseId: String = "",
    val modelStatus: ModelStatus? = null,
) {
    /**
     * Returns the first non-thought text part from any candidate, if present.
     */
    fun getText(): String? =
        candidates
            .asSequence()
            .mapNotNull { candidate ->
                candidate.content.parts
                    ?.firstOrNull { it.text != null && it.thought != true }
                    ?.text
            }.firstOrNull()

    /**
     * Returns the first thought text part from any candidate, if present.
     */
    fun getThought(): String? =
        candidates
            .asSequence()
            .mapNotNull { candidate ->
                candidate.content.parts
                    ?.firstOrNull { it.text != null && it.thought == true }
                    ?.text
            }.firstOrNull()
}
