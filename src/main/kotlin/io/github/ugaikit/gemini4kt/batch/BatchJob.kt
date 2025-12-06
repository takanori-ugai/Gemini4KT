package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.GeminiError
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the response from a batch job creation or retrieval.
 *
 * @property name The resource name of the batch job.
 * @property displayName The display name of the batch job.
 * @property state The current state of the batch job.
 * @property dest The destination of the batch job results.
 * @property error Error details if the batch job failed.
 * @property createTime The timestamp when the batch job was created.
 * @property updateTime The timestamp when the batch job was last updated.
 */
@Serializable
data class BatchJob(
    val name: String,
    @SerialName("display_name")
    val displayName: String? = null,
    val state: String, // e.g., JOB_STATE_SUCCEEDED, JOB_STATE_PENDING
    val dest: BatchJobDestination? = null,
    val error: GeminiError? = null,
    @SerialName("create_time")
    val createTime: String? = null,
    @SerialName("update_time")
    val updateTime: String? = null,
)
