package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Media resolution for tokenization.
 *
 * @property level The tokenization quality used for given media.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class MediaResolution(
    val level: MediaResolutionLevel? = null,
)
