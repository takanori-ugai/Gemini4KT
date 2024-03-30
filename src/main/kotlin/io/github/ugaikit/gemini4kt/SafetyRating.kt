package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a safety assessment for a specific category of potential harm.
 *
 * This data class is used to convey the likelihood or probability of a piece of content
 * being associated with a particular harm category, such as harassment, hate speech,
 * sexually explicit material, or dangerous content. The assessment helps in understanding
 * the content's compliance with safety standards and guidelines.
 *
 * @property category The [HarmCategory] enum indicating the type of potential harm being assessed.
 * @property probability A [String] value representing the assessed probability or likelihood
 *                       that the content falls into the specified harm category. Common values
 *                       might include descriptors like "NEGLIGIBLE", "LOW", "MEDIUM", or "HIGH".
 */
@Serializable
data class SafetyRating(
    val category: HarmCategory,
    val probability: HarmProbability,
    val blocked: Boolean? = null,
)
