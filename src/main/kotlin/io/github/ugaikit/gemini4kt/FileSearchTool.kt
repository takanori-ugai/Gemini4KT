package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a File Search tool.
 *
 * @property fileSearchStoreNames List of file search store names.
 * @property metadataFilter Optional. Metadata filter.
 */
@Serializable
data class FileSearchTool(
    @SerialName("file_search_store_names")
    val fileSearchStoreNames: List<String>? = null,
    @SerialName("metadata_filter")
    val metadataFilter: String? = null,
)
