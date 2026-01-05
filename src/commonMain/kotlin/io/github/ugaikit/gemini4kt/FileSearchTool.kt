package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a File Search tool.
 *
 * @property fileSearchStoreNames List of file search store names.
 * @property metadataFilter Optional. Metadata filter.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class FileSearchTool(
    @SerialName("file_search_store_names")
    val fileSearchStoreNames: Array<String>? = null,
    @SerialName("metadata_filter")
    val metadataFilter: String? = null,
)
