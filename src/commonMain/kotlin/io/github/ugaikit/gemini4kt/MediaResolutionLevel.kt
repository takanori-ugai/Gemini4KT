package io.github.ugaikit.gemini4kt

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * The media resolution level.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class MediaResolutionLevel {
    MEDIA_RESOLUTION_UNSPECIFIED,
    MEDIA_RESOLUTION_LOW,
    MEDIA_RESOLUTION_MEDIUM,
    MEDIA_RESOLUTION_HIGH,
    MEDIA_RESOLUTION_ULTRA_HIGH,
}
