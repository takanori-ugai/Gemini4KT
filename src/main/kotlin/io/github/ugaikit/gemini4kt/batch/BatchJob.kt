package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.GeminiError
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the response from a batch job creation or retrieval.
 * This corresponds to the `Operation` resource in the API.
 *
 * @property name The server-assigned name of the operation (e.g., "operations/...").
 *                Note: In some contexts, this might be "batches/..." if the API returns the resource directly,
 *                but the documentation suggests it returns an Operation.
 * @property metadata Service-specific metadata associated with the operation.
 * @property done If the value is false, it means the operation is still in progress.
 * @property error The error result of the operation in case of failure or cancellation.
 * @property response The normal, successful response of the operation.
 */
@Serializable
data class BatchJob(
    val name: String,
    val metadata: BatchJobMetadata? = null,
    val done: Boolean? = null,
    val error: GeminiError? = null,
    val response: BatchJobResponse? = null,
)

/**
 * Metadata for a batch job.
 *
 * @property model The model used for the batch job.
 * @property displayName A display name for the batch job.
 * @property output The output of the batch job.
 * @property createTime The timestamp when the batch job was created.
 * @property endTime The timestamp when the batch job ended.
 * @property updateTime The timestamp when the batch job was last updated.
 * @property batchStats Stats about the batch.
 * @property state The current state of the batch job.
 * @property name The resource name of the batch job.
 */
@Serializable
data class BatchJobMetadata(
    val model: String? = null,
    val displayName: String? = null,
    val output: BatchOutput? = null,
    val createTime: String? = null,
    val endTime: String? = null,
    val updateTime: String? = null,
    val batchStats: BatchStats? = null,
    val state: String? = null,
    val name: String? = null,
)

/**
 * The output of the batch job.
 *
 * @property inlinedResponses The list of inline responses.
 */
@Serializable
data class BatchOutput(
    val inlinedResponses: InlinedResponsesWrapper? = null,
)

/**
 * Wrapper for the list of inline responses.
 *
 * @property inlinedResponses The list of inline responses.
 */
@Serializable
data class InlinedResponsesWrapper(
    val inlinedResponses: List<BatchInlineResponse>? = null,
)

/**
 * Stats about the batch.
 *
 * @property requestCount The number of requests in the batch.
 * @property successfulRequestCount The number of successful requests in the batch.
 */
@Serializable
data class BatchStats(
    val requestCount: Int? = null,
    val successfulRequestCount: Int? = null,
)

/**
 * The response payload of a completed batch job.
 *
 * @property inlinedResponses The list of inline responses.
 */
@Serializable
data class BatchJobResponse(
    @SerialName("inlinedResponses")
    val inlinedResponses: InlinedResponsesWrapper? = null,
)
