package io.github.ugaikit.gemini4kt.batch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the destination of the batch job results.
 *
 * @property fileName The file name where results are stored (if using File API).
 * @property inlinedResponses The list of inline responses (if using inline requests).
 */
@Serializable
data class BatchJobDestination(
    @SerialName("file_name")
    val fileName: String? = null,
    @SerialName("inlined_responses")
    val inlinedResponses: List<BatchInlineResponse>? = null,
)
