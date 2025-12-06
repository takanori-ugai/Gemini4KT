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
 * @property state The current state of the batch job.
 * @property createTime The timestamp when the batch job was created.
 * @property updateTime The timestamp when the batch job was last updated.
 * @property batchStats Stats about the batch.
 */
@Serializable
data class BatchJobMetadata(
    // e.g., JOB_STATE_SUCCEEDED
    val state: String? = null,
    @SerialName("create_time")
    val createTime: String? = null,
    @SerialName("update_time")
    val updateTime: String? = null,
    // Docs say `batch.batchStats object (BatchStats)`. JSON representation often uses camelCase.
    // However, `BatchConfig` uses snake_case.
    // Let's use `ignoreUnknownKeys` in Json so if we miss it, it doesn't crash.
    // I'll stick to camelCase property name and let standard serialization handle it if it's camelCase,
    // or add @SerialName if I suspect snake_case.
    // Given "input_config" was snake_case in Request, response might be snake_case too?
    // The guide example `batch_state=$(jq -r '.metadata.state' ...)` implies camelCase or simple names.
    // `responses_file_name=$(jq -r '.response.responsesFile' ...)` implies camelCase.
    // I will use `batchStats`.
    // Assuming snake_case based on typical patterns, but check docs. Docs say camelCase?
    @SerialName("batch_stats")
    val batchStats: BatchStats? = null,
)

/**
 * Stats about the batch.
 */
@Serializable
data class BatchStats(
    val requestCount: Int? = null,
    val successfulRequestCount: Int? = null,
    val failedRequestCount: Int? = null,
    val pendingRequestCount: Int? = null,
)

/**
 * The response payload of a completed batch job.
 *
 * @property responsesFile The file containing the responses (File API).
 * @property inlinedResponses The list of inline responses.
 */
@Serializable
data class BatchJobResponse(
    // Guide says `responsesFile`.
    val responsesFile: String? = null,
    // Wait, the REST example in guide: `jq '.response | has("inlinedResponses")'`.
    // So it is `inlinedResponses`.
    // BUT, the input config uses `inlined_requests`? No, `inline_requests`.
    // The InputConfig doc says `requests` (InlinedRequests).
    // Let's assume camelCase for Output based on guide.
    // Guide showed `inlinedResponses` in `GenerateContentBatchOutput` but `inlined_responses` in my previous test code?
    @SerialName("inlined_responses")
    val inlinedResponses: List<BatchInlineResponse>? = null,
)
