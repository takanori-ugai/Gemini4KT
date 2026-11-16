package io.github.ugaikit.gemini4kt.decisions

import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

private val SUPPORTED_CONTENT_TYPES = setOf("text", "image", "audio", "video", "document")

/** Shared decision context, or text-only question instructions, in the API's unwrapped JSON shape. */
@Serializable(with = DecisionInputSerializer::class)
data class DecisionInput(
    val value: JsonElement,
) {
    constructor(text: String) : this(JsonPrimitive(text))

    constructor(content: InteractionContent) : this(Json.encodeToJsonElement(content))

    constructor(contents: List<InteractionContent>) : this(Json.encodeToJsonElement(contents))

    init {
        if (value is JsonPrimitive) {
            require(value.isString) { "Decision input must be a string or content blocks." }
        } else {
            require(blocks().all { (it["type"] as? JsonPrimitive)?.content in SUPPORTED_CONTENT_TYPES }) {
                "Decision input supports only text, image, audio, video, and document blocks."
            }
        }
    }

    internal fun requireTextOnly() {
        require(value is JsonPrimitive || blocks().all { it["type"] == JsonPrimitive("text") }) {
            "Decision question instructions must contain text only."
        }
    }

    private fun blocks(): List<JsonObject> =
        when (value) {
            is JsonObject -> listOf(value)
            is JsonArray -> {
                require(value.isNotEmpty()) { "Decision input must contain at least one content block." }
                value.map { it as? JsonObject ?: throw IllegalArgumentException("Expected a decision content object.") }
            }
            else -> throw IllegalArgumentException("Expected a decision content object or array.")
        }
}

/** Serializes decision input without adding a wrapper or class discriminator. */
object DecisionInputSerializer : KSerializer<DecisionInput> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: DecisionInput,
    ) {
        val json = encoder as? JsonEncoder ?: throw SerializationException("DecisionInput requires JSON.")
        json.encodeJsonElement(value.value)
    }

    override fun deserialize(decoder: Decoder): DecisionInput {
        val json = decoder as? JsonDecoder ?: throw SerializationException("DecisionInput requires JSON.")
        return DecisionInput(json.decodeJsonElement())
    }
}
