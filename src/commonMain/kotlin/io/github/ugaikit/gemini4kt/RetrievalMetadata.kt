package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Metadata related to retrieval in the grounding flow.
 *
 * @property googleSearchDynamicRetrievalScore Score indicating how likely Google Search could help answer.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class RetrievalMetadata(
    val googleSearchDynamicRetrievalScore: Double? = null,
)
