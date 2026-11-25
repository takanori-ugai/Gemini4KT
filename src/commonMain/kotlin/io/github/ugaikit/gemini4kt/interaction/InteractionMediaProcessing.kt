package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/** Processing strategy supplied on an Interactions content block. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable(with = InteractionMediaProcessingSerializer::class)
sealed interface InteractionMediaProcessing

/**
 * Static processing for a document, spreadsheet, video, or audio clip.
 *
 * Use [pageOptions] or [spreadsheetOptions] for documents. For video or audio,
 * supply offsets as second strings, for example `"10.5s"`. [fps] and
 * [contentLayers] apply to video, with layers `frames`, `audio`, or `transcript`.
 */
@OptIn(ExperimentalJsExport::class, ExperimentalSerializationApi::class)
@JsExport
@Serializable
data class InteractionStaticProcessing(
    @SerialName("page_options") val pageOptions: InteractionPageOptions? = null,
    @SerialName("spreadsheet_options") val spreadsheetOptions: InteractionSpreadsheetOptions? = null,
    @SerialName("start_offset") val startOffset: String? = null,
    @SerialName("end_offset") val endOffset: String? = null,
    val fps: Double? = null,
    @SerialName("content_layers") val contentLayers: List<String>? = null,
) : InteractionMediaProcessing {
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val type: String = "static"
}

/** Inclusive, one-based page range with optional `page_image` and `page_text` layers. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionPageOptions(
    @SerialName("start_page") val startPage: Int? = null,
    @SerialName("end_page") val endPage: Int? = null,
    @SerialName("content_layers") val contentLayers: List<String>? = null,
)

/** One-based worksheet index, optional A1 cell range, and `data`, `formulas`, or `visual` layers. */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionSpreadsheetOptions(
    @SerialName("sheet_index") val sheetIndex: Int? = null,
    @SerialName("visible_cells") val visibleCells: String? = null,
    @SerialName("content_layers") val contentLayers: List<String>? = null,
)

internal object InteractionMediaProcessingSerializer : KSerializer<InteractionMediaProcessing> {
    override val descriptor = InteractionStaticProcessing.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: InteractionMediaProcessing,
    ) {
        when (value) {
            is InteractionVideoProcessing ->
                encoder.encodeSerializableValue(InteractionVideoProcessing.serializer(), value)
            is InteractionStaticProcessing ->
                encoder.encodeSerializableValue(InteractionStaticProcessing.serializer(), value)
        }
    }

    override fun deserialize(decoder: Decoder): InteractionMediaProcessing {
        val jsonDecoder =
            decoder as? JsonDecoder ?: throw SerializationException("Media processing requires JSON.")
        val json = jsonDecoder.json
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> json.decodeFromJsonElement(InteractionVideoProcessing.serializer(), element)
            is JsonObject -> {
                when (element["type"]) {
                    JsonPrimitive("static") ->
                        json.decodeFromJsonElement(InteractionStaticProcessing.serializer(), element)
                    JsonPrimitive("agentic") -> InteractionVideoProcessing.AGENTIC
                    else -> throw SerializationException("Unsupported media processing type.")
                }
            }
            else -> throw SerializationException("Expected a media processing string or object.")
        }
    }
}
