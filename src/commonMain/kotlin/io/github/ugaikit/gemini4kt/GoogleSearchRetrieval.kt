package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Tool to retrieve public web data for grounding, powered by Google.
 *
 * @property dynamicRetrievalConfig Specifies the dynamic retrieval configuration for the given source.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class GoogleSearchRetrieval(
    val dynamicRetrievalConfig: DynamicRetrievalConfig? = null,
)
