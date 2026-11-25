package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/** Inclusive, one-based page range with optional `PAGE_IMAGE` and `PAGE_TEXT` layers. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class DocumentMetadata(
    val startPage: Int? = null,
    val endPage: Int? = null,
    val contentLayers: List<String>? = null,
)

/** One-based worksheet index, optional A1 cell range, and `DATA`, `FORMULAS`, or `VISUAL` layers. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class SpreadsheetMetadata(
    val sheetIndex: Int? = null,
    val visibleCells: String? = null,
    val contentLayers: List<String>? = null,
)

/** Audio clip bounds as second strings, for example `"10.5s"`. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class AudioMetadata(
    val startOffset: String? = null,
    val endOffset: String? = null,
)
