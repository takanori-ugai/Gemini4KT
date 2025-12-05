package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.Serializable

/**
 * Response from fileSearchStores.list containing a paginated list of FileSearchStores.
 *
 * @property fileSearchStores The returned fileSearchStores.
 * @property nextPageToken A token, which can be sent as pageToken to retrieve the next page.
 */
@Serializable
data class ListFileSearchStoresResponse(
    val fileSearchStores: List<FileSearchStore>? = null,
    val nextPageToken: String? = null
)
