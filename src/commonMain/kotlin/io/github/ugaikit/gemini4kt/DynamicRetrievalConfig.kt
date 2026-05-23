package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Describes the options to customize dynamic retrieval.
 *
 * @property mode The mode of the predictor to be used in dynamic retrieval.
 * @property dynamicThreshold The threshold to be used in dynamic retrieval.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class DynamicRetrievalConfig(
    val mode: DynamicRetrievalMode? = null,
    val dynamicThreshold: Double? = null,
)

/**
 * The mode of the predictor to be used in dynamic retrieval.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class DynamicRetrievalMode {
    MODE_UNSPECIFIED,
    MODE_DYNAMIC,
}
