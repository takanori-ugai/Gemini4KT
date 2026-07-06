package io.github.ugaikit.gemini4kt.interaction

import io.github.ugaikit.gemini4kt.MediaResolution
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Image content attached to an interaction response.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionImageContent(
    val type: String? = null,
    val data: String? = null,
    val uri: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    val resolution: MediaResolution? = null,
)

/**
 * Audio content attached to an interaction response.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionAudioContent(
    val type: String? = null,
    val data: String? = null,
    val uri: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    val channels: Int? = null,
    @SerialName("sample_rate") val sampleRate: Int? = null,
)

/**
 * Video content attached to an interaction response.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionVideoContent(
    val type: String? = null,
    val data: String? = null,
    val uri: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    val resolution: MediaResolution? = null,
)
