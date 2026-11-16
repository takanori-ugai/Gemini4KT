package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/** Video understanding strategy for Generate Content inputs and function response parts. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class MediaProcessing {
    MEDIA_PROCESSING_UNSPECIFIED,
    STATIC,
    AGENTIC,
}
