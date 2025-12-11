package io.github.ugaikit.gemini4kt.batch

import kotlinx.serialization.Serializable

/**
 * Represents inline requests for a batch job.
 *
 * @property requests A list of individual batch requests.
 */
@Serializable
data class BatchRequestInput(
    val requests: List<BatchItemRequest>,
)
