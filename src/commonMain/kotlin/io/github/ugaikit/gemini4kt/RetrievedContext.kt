package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Chunk from context retrieved by the file search tool.
 *
 * @property customMetadata User-provided metadata about the retrieved context.
 * @property uri URI reference of the semantic retrieval document.
 * @property title Title of the document.
 * @property text Text of the chunk.
 * @property fileSearchStore Name of the FileSearchStore containing the document.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class RetrievedContext(
    val customMetadata: List<CustomMetadata>? = null,
    val uri: String? = null,
    val title: String? = null,
    val text: String? = null,
    val fileSearchStore: String? = null,
)
