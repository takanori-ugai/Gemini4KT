package io.github.ugaikit.gemini4kt.batch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the configuration for a batch job.
 *
 * @property displayName An optional display name for the batch job.
 * @property inputConfig The input configuration for the batch job.
 */
@Serializable
data class BatchConfig(
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("input_config")
    val inputConfig: BatchInputConfig,
)
