package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a model with its metadata and configuration parameters, providing
 * detailed information about its capabilities and restrictions.
 *
 * @property name The unique identifier for the model.
 * @property version The version of the model, indicating its iteration or update
 * status.
 * @property displayName A human-readable name for the model, intended for display
 * purposes.
 * @property description A brief description of the model, outlining its purpose,
 * use case, or capabilities.
 * @property inputTokenLimit The maximum number of tokens that can be provided as
 * input to the model.
 * @property outputTokenLimit The maximum number of tokens that the model can
 * generate as output.
 * @property supportedGenerationMethods A list of strings indicating the generation
 * methods supported by the model (e.g., "text", "code").
 * @property temperature An optional parameter influencing the randomness of the
 * output. Lower values make the model more deterministic.
 * @property topP An optional parameter controlling the nucleus sampling method,
 * where a cumulative probability threshold is set for token selection.
 * @property topK An optional parameter that restricts the model to only consider
 * the top-k most likely tokens for each step of generation.
 */
@Serializable
data class Model(
    val name: String,
    val baseModelId: String? = null,
    val version: String,
    val displayName: String,
    val description: String? = null,
    val inputTokenLimit: Int,
    val outputTokenLimit: Int,
    val supportedGenerationMethods: List<String>,
    val temperature: Double? = null,
    val maxTemperature: Double? = null,
    val topP: Double? = null,
    val topK: Int? = null,
)
