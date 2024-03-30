package io.github.uaikit

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
    val safetyRatings: List<SafetyRating>,
)
