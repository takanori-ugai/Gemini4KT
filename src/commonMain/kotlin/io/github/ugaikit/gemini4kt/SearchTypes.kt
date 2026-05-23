package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Different types of search that can be enabled on the GoogleSearch tool.
 *
 * @property webSearch Enables web search.
 * @property imageSearch Enables image search.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class SearchTypes(
    val webSearch: WebSearch? = null,
    val imageSearch: ImageSearch? = null,
)
