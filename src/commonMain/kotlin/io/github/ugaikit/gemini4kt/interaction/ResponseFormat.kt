@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Response format configuration used by the Interactions API.
 *
 * The API currently supports text, audio, and image response formats.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable(with = ResponseFormatSerializer::class)
sealed interface ResponseFormat {
    val type: String
}

/**
 * Response format for text output.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class TextResponseFormat(
    @Required
    override val type: String = "text",
    val mimeType: String? = null,
    val schema: JsonElement? = null,
) : ResponseFormat

/**
 * Response format for audio output.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class AudioResponseFormat(
    @Required
    override val type: String = "audio",
    val mimeType: String? = null,
    val delivery: String? = null,
    val sampleRate: Int? = null,
    val bitRate: Int? = null,
) : ResponseFormat

/**
 * Response format for image output.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ImageResponseFormat(
    @Required
    override val type: String = "image",
    val mimeType: String? = null,
    val delivery: String? = null,
    val aspectRatio: String? = null,
    val imageSize: String? = null,
) : ResponseFormat

/**
 * Serializes and deserializes response format unions.
 */
object ResponseFormatSerializer : KSerializer<ResponseFormat> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("io.github.ugaikit.gemini4kt.interaction.ResponseFormat")

    override fun serialize(
        encoder: Encoder,
        value: ResponseFormat,
    ) {
        when (value) {
            is TextResponseFormat -> encoder.encodeSerializableValue(TextResponseFormat.serializer(), value)
            is AudioResponseFormat -> encoder.encodeSerializableValue(AudioResponseFormat.serializer(), value)
            is ImageResponseFormat -> encoder.encodeSerializableValue(ImageResponseFormat.serializer(), value)
        }
    }

    override fun deserialize(decoder: Decoder): ResponseFormat {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("ResponseFormat can only be deserialized as JSON.")

        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonObject) {
            throw SerializationException("ResponseFormat must be a JSON object.")
        }

        val type =
            (element["type"] as? JsonPrimitive)?.content
                ?: throw SerializationException("ResponseFormat.type is required.")

        return when (type) {
            "text" -> jsonDecoder.json.decodeFromJsonElement(TextResponseFormat.serializer(), element)
            "audio" -> jsonDecoder.json.decodeFromJsonElement(AudioResponseFormat.serializer(), element)
            "image" -> jsonDecoder.json.decodeFromJsonElement(ImageResponseFormat.serializer(), element)
            else -> throw SerializationException("Unknown ResponseFormat type: $type")
        }
    }
}
