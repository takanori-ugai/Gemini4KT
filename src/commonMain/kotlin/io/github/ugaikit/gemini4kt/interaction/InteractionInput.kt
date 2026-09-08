@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package io.github.ugaikit.gemini4kt.interaction

import io.github.ugaikit.gemini4kt.Content
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Typed input payload accepted by the Interactions API.
 *
 * The API accepts either a single string, a single content object, arrays of content,
 * arrays of steps, arrays of turns, or raw JSON for less common shapes.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable(with = InteractionInputSerializer::class)
sealed interface InteractionInput {
    /**
     * String prompt input.
     */
    @Serializable
    data class Text(
        val value: String,
    ) : InteractionInput

    /**
     * A single content object.
     */
    @Serializable
    data class SingleContent(
        val value: Content,
    ) : InteractionInput

    /**
     * A single typed interaction content item, such as a video input.
     */
    @Serializable
    data class SingleInteractionContent(
        val value: InteractionContent,
    ) : InteractionInput

    /**
     * Multiple content objects.
     */
    @Serializable
    data class ContentList(
        val value: Array<Content>,
    ) : InteractionInput {
        override fun equals(other: Any?): Boolean = other is ContentList && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()
    }

    /**
     * Multiple typed interaction content items, such as text and video inputs.
     */
    @Serializable
    data class InteractionContentList(
        val value: Array<InteractionContent>,
    ) : InteractionInput {
        override fun equals(other: Any?): Boolean = other is InteractionContentList && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()
    }

    /**
     * Multiple turns.
     */
    @Serializable
    data class TurnList(
        val value: Array<InteractionTurn>,
    ) : InteractionInput {
        override fun equals(other: Any?): Boolean = other is TurnList && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()
    }

    /**
     * Multiple steps.
     */
    @Serializable
    data class StepList(
        val value: Array<InteractionStep>,
    ) : InteractionInput {
        override fun equals(other: Any?): Boolean = other is StepList && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()
    }

    /**
     * Raw JSON fallback for shapes not represented as explicit data classes yet.
     */
    @Serializable
    data class RawJson(
        val value: JsonElement,
    ) : InteractionInput
}

/**
 * Serializes and deserializes typed interaction input payloads.
 */
object InteractionInputSerializer : KSerializer<InteractionInput> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("io.github.ugaikit.gemini4kt.interaction.InteractionInput")

    override fun serialize(
        encoder: Encoder,
        value: InteractionInput,
    ) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: throw SerializationException("InteractionInput can only be serialized as JSON.")

        when (value) {
            is InteractionInput.Text -> jsonEncoder.encodeString(value.value)
            is InteractionInput.SingleContent -> jsonEncoder.encodeSerializableValue(Content.serializer(), value.value)
            is InteractionInput.SingleInteractionContent ->
                jsonEncoder.encodeSerializableValue(InteractionContent.serializer(), value.value)
            is InteractionInput.ContentList ->
                jsonEncoder.encodeSerializableValue(ArraySerializer(Content.serializer()), value.value)
            is InteractionInput.InteractionContentList ->
                jsonEncoder.encodeSerializableValue(ArraySerializer(InteractionContent.serializer()), value.value)
            is InteractionInput.StepList ->
                jsonEncoder.encodeSerializableValue(ArraySerializer(InteractionStep.serializer()), value.value)
            is InteractionInput.TurnList ->
                jsonEncoder.encodeSerializableValue(ArraySerializer(InteractionTurn.serializer()), value.value)
            is InteractionInput.RawJson -> jsonEncoder.encodeJsonElement(value.value)
        }
    }

    override fun deserialize(decoder: Decoder): InteractionInput {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("InteractionInput can only be deserialized as JSON.")

        val element = jsonDecoder.decodeJsonElement()
        return element.toInteractionInput(jsonDecoder)
    }
}

/**
 * Converts a JSON element into a typed interaction input payload.
 */
internal fun JsonElement.toInteractionInput(jsonDecoder: JsonDecoder? = null): InteractionInput =
    when (this) {
        is JsonPrimitive ->
            if (isString) {
                InteractionInput.Text(content)
            } else {
                InteractionInput.RawJson(this)
            }
        is JsonObject -> {
            if (jsonDecoder != null && ("parts" in this || "role" in this)) {
                val json = jsonDecoder
                runCatching {
                    InteractionInput.SingleContent(json.json.decodeFromJsonElement(Content.serializer(), this))
                }.getOrElse {
                    InteractionInput.RawJson(this)
                }
            } else if (jsonDecoder != null && "type" in this) {
                val json = jsonDecoder
                runCatching {
                    InteractionInput.SingleInteractionContent(
                        json.json.decodeFromJsonElement(InteractionContent.serializer(), this),
                    )
                }.getOrElse {
                    InteractionInput.RawJson(this)
                }
            } else {
                InteractionInput.RawJson(this)
            }
        }
        is JsonArray -> {
            if (isEmpty() || jsonDecoder == null) {
                InteractionInput.ContentList(emptyArray())
            } else {
                val firstObject = firstOrNull() as? JsonObject
                if (firstObject == null) {
                    InteractionInput.RawJson(this)
                } else {
                    when {
                        "type" in firstObject && "content" in firstObject -> {
                            val json = jsonDecoder
                            runCatching {
                                InteractionInput.StepList(
                                    json.json.decodeFromJsonElement(
                                        ArraySerializer(InteractionStep.serializer()),
                                        this,
                                    ),
                                )
                            }.getOrElse {
                                InteractionInput.RawJson(this)
                            }
                        }
                        "role" in firstObject && "content" in firstObject -> {
                            val json = jsonDecoder
                            runCatching {
                                InteractionInput.TurnList(
                                    json.json.decodeFromJsonElement(
                                        ArraySerializer(InteractionTurn.serializer()),
                                        this,
                                    ),
                                )
                            }.getOrElse {
                                InteractionInput.RawJson(this)
                            }
                        }
                        "type" in firstObject -> {
                            val json = jsonDecoder
                            runCatching {
                                InteractionInput.InteractionContentList(
                                    json.json.decodeFromJsonElement(
                                        ArraySerializer(InteractionContent.serializer()),
                                        this,
                                    ),
                                )
                            }.getOrElse {
                                InteractionInput.RawJson(this)
                            }
                        }
                        "parts" in firstObject || "role" in firstObject -> {
                            val json = jsonDecoder
                            runCatching {
                                InteractionInput.ContentList(
                                    json.json.decodeFromJsonElement(
                                        ArraySerializer(Content.serializer()),
                                        this,
                                    ),
                                )
                            }.getOrElse {
                                InteractionInput.RawJson(this)
                            }
                        }
                        else -> InteractionInput.RawJson(this)
                    }
                }
            }
        }
    }

/**
 * Creates a typed text input.
 */
fun interactionInput(value: String): InteractionInput = InteractionInput.Text(value)

/**
 * Creates a typed input from a single content object.
 */
fun interactionInput(value: Content): InteractionInput = InteractionInput.SingleContent(value)

/**
 * Creates a typed interaction content input, for example a video with agentic processing.
 */
fun interactionInput(value: InteractionContent): InteractionInput = InteractionInput.SingleInteractionContent(value)

fun interactionContentInput(value: Array<InteractionContent>): InteractionInput = InteractionInput.InteractionContentList(value)

fun interactionStepsInput(value: Array<InteractionStep>): InteractionInput = InteractionInput.StepList(value)

fun interactionTurnsInput(value: Array<InteractionTurn>): InteractionInput = InteractionInput.TurnList(value)
