package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents feedback on the safety of a prompt, including ratings across various harm categories.
 *
 * This data class is used to encapsulate safety-related feedback for a given piece of content or prompt,
 * providing detailed insights into its perceived safety across different dimensions. Each [SafetyRating]
 * in the list assesses a specific aspect of content safety, such as the likelihood of containing hate speech,
 * harassment, sexually explicit material, or dangerous content.
 *
 * @property safetyRatings A list of [SafetyRating] objects, each representing a safety assessment
 *                         for a different category of potential harm.
 */
@Serializable
data class PromptFeedback(
    val safetyRatings: List<SafetyRating> = emptyList(),
)

/**
 * Represents the prompt feedback builder.
 */
class PromptFeedbackBuilder {
    /**
     * Holds the safety ratings.
     */
    private val safetyRatings: MutableList<SafetyRating> = mutableListOf()

    /**
     * Handles safety rating.
     *
     * @param init The init.
     */
    fun safetyRating(init: SafetyRatingBuilder.() -> Unit) {
        safetyRatings.add(SafetyRatingBuilder().apply(init).build())
    }

    /**
     * Handles build.
     */
    fun build() = PromptFeedback(safetyRatings)
}

/**
 * Handles prompt feedback.
 *
 * @param init The init.
 */
fun promptFeedback(init: PromptFeedbackBuilder.() -> Unit): PromptFeedback = PromptFeedbackBuilder().apply(init).build()
