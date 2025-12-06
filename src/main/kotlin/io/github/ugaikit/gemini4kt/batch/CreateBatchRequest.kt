package io.github.ugaikit.gemini4kt.batch

import kotlinx.serialization.Serializable

/**
 * Represents a request to create a batch job.
 *
 * @property batch The configuration for the batch job.
 */
@Serializable
data class CreateBatchRequest(
    val batch: BatchConfig,
)
