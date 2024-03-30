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
 */
@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>,
    val promptFeedback: PromptFeedback? = null,
)
