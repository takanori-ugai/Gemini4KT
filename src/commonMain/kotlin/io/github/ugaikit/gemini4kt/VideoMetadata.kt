package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Metadata describing the input video content.
 *
 * @property startOffset The start offset of the video.
 * @property endOffset The end offset of the video.
 * @property fps The frame rate of the video sent to the model.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class VideoMetadata(
    val startOffset: String? = null,
    val endOffset: String? = null,
    val fps: Double? = null,
)
