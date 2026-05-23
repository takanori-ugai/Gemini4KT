package io.github.ugaikit.gemini4kt

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * The type of tool in the function call.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ToolType {
    TOOL_TYPE_UNSPECIFIED,
    GOOGLE_SEARCH_WEB,
    GOOGLE_SEARCH_IMAGE,
    URL_CONTEXT,
    GOOGLE_MAPS,
    FILE_SEARCH,
}
