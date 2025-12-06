package io.github.ugaikit.gemini4kt.batch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the configuration for a batch job.
 *
 * @property model The name of the Model to use for generating the completion. Format: models/{model}.
 * @property displayName An optional display name for the batch job.
 * @property inputConfig The input configuration for the batch job.
 */
@Serializable
data class BatchConfig(
    val model: String? = null,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("input_config")
    val inputConfig: BatchInputConfig,
)
