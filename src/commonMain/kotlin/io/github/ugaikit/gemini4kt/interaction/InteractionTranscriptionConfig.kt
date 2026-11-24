package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/** Configuration for unary transcription with the Interactions API. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionTranscriptionConfig(
    @Serializable(with = InteractionTranscriptionModeSerializer::class)
    val mode: InteractionTranscriptionMode? = null,
    @SerialName("language_codes") val languageCodes: List<String>? = null,
    @SerialName("custom_vocabulary") val customVocabulary: List<String>? = null,
)

/**
 * Controls whether the model transcribes speech verbatim or supports spoken edits.
 *
 * @property sourceText Optional initial text to edit in Smart mode, up to 64 KiB in UTF-8.
 * For sequential edits, pass the previous request's resulting text here.
 * @property timestampGranularities Set to a list containing `word` for word timestamps in verbatim mode.
 * @property diarizationMode Set to `speaker` for speaker labels in verbatim mode.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionTranscriptionMode(
    val type: InteractionTranscriptionModeType,
    @SerialName("source_text") val sourceText: String? = null,
    @SerialName("timestamp_granularities") val timestampGranularities: List<String>? = null,
    @SerialName("diarization_mode") val diarizationMode: String? = null,
) {
    init {
        require(sourceText == null || type == InteractionTranscriptionModeType.SMART) {
            "sourceText requires Smart transcription mode."
        }
        val hasVerbatimOptions = timestampGranularities != null || diarizationMode != null
        require(!hasVerbatimOptions || type == InteractionTranscriptionModeType.VERBATIM) {
            "Timestamps and diarization require verbatim transcription mode."
        }
    }
}

/** Mode identifiers for the Interactions transcription API. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class InteractionTranscriptionModeType {
    @SerialName("smart")
    SMART,

    @SerialName("verbatim")
    VERBATIM,
}

internal object InteractionTranscriptionModeSerializer :
    JsonTransformingSerializer<InteractionTranscriptionMode>(InteractionTranscriptionMode.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val shorthand = element as? JsonPrimitive
        return if (shorthand?.isString == true) JsonObject(mapOf("type" to shorthand)) else element
    }
}
