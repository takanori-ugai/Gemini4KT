package io.github.ugaikit.gemini4kt

/**
 * Enumerates the modalities that can be returned by the model.
 *
 * @property MODALITY_UNSPECIFIED Default value.
 * @property TEXT Indicates the model should return text.
 * @property IMAGE Indicates the model should return images.
 * @property AUDIO Indicates the model should return audio.
 */
enum class Modality {
    MODALITY_UNSPECIFIED,
    TEXT,
    IMAGE,
    AUDIO,
}
