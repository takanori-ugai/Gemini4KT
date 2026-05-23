package io.github.ugaikit.gemini4kt

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Enumerates the modalities that can be returned by the model.
 *
 * @property MODALITY_UNSPECIFIED Default value.
 * @property TEXT Indicates the model should return text.
 * @property IMAGE Indicates the model should return images.
 * @property AUDIO Indicates the model should return audio.
 * @property VIDEO Indicates the model should return video.
 * @property DOCUMENT Indicates the model should return document.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class Modality {
    MODALITY_UNSPECIFIED,
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO,
    DOCUMENT,
}
