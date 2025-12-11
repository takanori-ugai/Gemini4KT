package io.github.ugaikit.gemini4kt.batch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the input configuration for a batch job.
 *
 * @property gcsSource The Google Cloud Storage source for the input data.
 * @property fileName The file name of the input data (File API).
 * @property requests Inline requests for the batch job.
 */
@Serializable
data class BatchInputConfig(
    @SerialName("gcs_source")
    val gcsSource: BatchGcsSource? = null,
    @SerialName("file_name")
    val fileName: String? = null,
    val requests: BatchRequestInput? = null,
)

/**
 * Represents a Google Cloud Storage source.
 *
 * @property uris A list of Google Cloud Storage URIs.
 */
@Serializable
data class BatchGcsSource(
    val uris: List<String>,
)
