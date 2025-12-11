package io.github.ugaikit.gemini4kt.batch

import kotlinx.serialization.Serializable

/**
 * Represents the response from listing batch jobs.
 *
 * @property batches The list of batch jobs.
 * @property nextPageToken A token to retrieve the next page of results.
 */
@Serializable
data class ListBatchesResponse(
    val operations: List<BatchJob>? = null,
    val nextPageToken: String? = null,
)
