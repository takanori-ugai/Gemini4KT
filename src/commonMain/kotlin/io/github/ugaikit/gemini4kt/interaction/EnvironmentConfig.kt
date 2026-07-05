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
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Typed environment configuration accepted by the Interactions API.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable(with = InteractionEnvironmentSerializer::class)
sealed interface InteractionEnvironment

/**
 * Remote environment configuration.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class EnvironmentConfig(
    @Required
    val type: String = "remote",
    val sources: Array<EnvironmentSource>? = null,
) : InteractionEnvironment {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EnvironmentConfig

        if (type != other.type) return false
        if (sources != null) {
            if (other.sources == null) return false
            if (!sources.contentEquals(other.sources)) return false
        } else if (other.sources != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (sources?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * A reference to an existing environment ID.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class EnvironmentReference(
    val id: String,
) : InteractionEnvironment

/**
 * A mounted source entry for a remote environment.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class EnvironmentSource(
    val type: String,
    val target: String? = null,
    val content: String? = null,
    val source: String? = null,
    val encoding: String? = null,
)

/**
 * Serializes environment unions as either a string ID or a remote environment object.
 */
object InteractionEnvironmentSerializer : KSerializer<InteractionEnvironment> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("io.github.ugaikit.gemini4kt.interaction.InteractionEnvironment")

    override fun serialize(
        encoder: Encoder,
        value: InteractionEnvironment,
    ) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: throw SerializationException("InteractionEnvironment can only be serialized as JSON.")

        when (value) {
            is EnvironmentReference -> jsonEncoder.encodeString(value.id)
            is EnvironmentConfig -> {
                val encoded = jsonEncoder.json.encodeToJsonElement(EnvironmentConfig.serializer(), value) as JsonObject
                jsonEncoder.encodeJsonElement(
                    buildJsonObject {
                        put("type", JsonPrimitive(value.type))
                        encoded.entries.forEach { (key, element) ->
                            if (key != "type") {
                                put(key, element)
                            }
                        }
                    },
                )
            }
        }
    }

    override fun deserialize(decoder: Decoder): InteractionEnvironment {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("InteractionEnvironment can only be deserialized as JSON.")

        val element = jsonDecoder.decodeJsonElement()
        return when (element) {
            is JsonPrimitive ->
                if (element.isString) {
                    EnvironmentReference(element.content)
                } else {
                    throw SerializationException("InteractionEnvironment string references must be strings.")
                }
            is JsonObject ->
                jsonDecoder.json.decodeFromJsonElement(EnvironmentConfig.serializer(), element)
            else -> throw SerializationException("InteractionEnvironment must be a string or object.")
        }
    }
}
