package io.github.ugaikit.gemini4kt

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Defines the stage of the underlying model.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ModelStage {
    MODEL_STAGE_UNSPECIFIED,
    UNSTABLE_EXPERIMENTAL,
    EXPERIMENTAL,
    PREVIEW,
    STABLE,
    LEGACY,
    DEPRECATED,
    RETIRED,
}
