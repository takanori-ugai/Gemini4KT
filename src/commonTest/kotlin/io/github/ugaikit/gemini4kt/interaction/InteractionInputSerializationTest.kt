package io.github.ugaikit.gemini4kt.interaction

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Part
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class InteractionInputSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }

    @Test
    fun textInputRoundTrips() {
        val input = InteractionInput.Text("hello")

        val encoded = json.encodeToString(InteractionInputSerializer, input)
        val decoded = json.decodeFromString(InteractionInputSerializer, encoded)

        assertEquals("\"hello\"", encoded)
        assertEquals(input, decoded)
    }

    @Test
    fun singleContentInputDecodesFromObjectShape() {
        val encoded =
            """
            {
              "parts": [{"text": "hello"}],
              "role": "user"
            }
            """.trimIndent()

        val decoded = json.decodeFromString(InteractionInputSerializer, encoded)

        assertTrue(decoded is InteractionInput.SingleContent)
        assertEquals("user", decoded.value.role)
        assertEquals(1, decoded.value.parts?.size)
        assertEquals(
            "hello",
            decoded.value.parts
                ?.firstOrNull()
                ?.text,
        )
    }

    @Test
    fun singleContentInputSerializesToObjectShape() {
        val input =
            InteractionInput.SingleContent(
                Content(parts = arrayOf(Part(text = "hello")), role = "user"),
            )

        val encoded = json.encodeToString(InteractionInputSerializer, input)

        assertTrue(encoded.contains("\"parts\""))
        assertTrue(encoded.contains("\"role\":\"user\""))
        assertEquals(
            json.decodeFromString<JsonElement>(encoded),
            json.decodeFromString<JsonElement>(
                """{"parts":[{"text":"hello"}],"role":"user"}""",
            ),
        )
    }

    @Test
    fun videoInputSerializesWithAgenticProcessing() {
        val input =
            interactionContentInput(
                arrayOf(
                    InteractionContent(type = "video", uri = "files/video", mimeType = "video/mp4", processing = InteractionVideoProcessing.AGENTIC),
                    InteractionContent(type = "text", text = "What are the three main arguments?"),
                ),
            )

        val encoded = json.encodeToString(InteractionInput.serializer(), input)

        assertTrue(encoded.contains("\"type\":\"video\""))
        assertTrue(encoded.contains("\"processing\":\"agentic\""))
        assertTrue(encoded.contains("\"mime_type\":\"video/mp4\""))

        val decoded = json.decodeFromString(InteractionInputSerializer, encoded)
        assertEquals(input, decoded)
        assertNotEquals(
            input,
            interactionContentInput(
                arrayOf(
                    InteractionContent(
                        type = "video",
                        uri = "files/video",
                        mimeType = "video/mp4",
                        processing = InteractionVideoProcessing.STATIC,
                    ),
                    InteractionContent(type = "text", text = "What are the three main arguments?"),
                ),
            ),
        )
    }

    @Test
    fun singleInteractionContentInputRoundTrips() {
        val input =
            interactionInput(
                InteractionContent(
                    type = "video",
                    uri = "files/video",
                    mimeType = "video/mp4",
                    processing = InteractionVideoProcessing.STATIC,
                ),
            )

        val encoded = json.encodeToString(InteractionInputSerializer, input)
        val decoded = json.decodeFromString(InteractionInputSerializer, encoded)

        assertEquals(input, decoded)
        assertTrue(encoded.contains("\"processing\":\"static\""))
    }

    @Test
    fun interactionStepEqualityCoversOptionalMetadataBranches() {
        val step =
            InteractionStep(
                type = "processing_result",
                content = arrayOf(InteractionContent(type = "text", text = "loaded")),
                id = "step_01",
                callId = "call_01",
                signature = "sig_01",
                summary = arrayOf(InteractionContent(type = "text", text = "summary")),
            )

        assertNotEquals(step, step.copy(id = "step_02"))
        assertNotEquals(step, step.copy(callId = "call_02"))
        assertNotEquals(step, step.copy(signature = "sig_02"))
        assertNotEquals(step, step.copy(summary = null))
        assertNotEquals(step.copy(summary = null), step)
        assertNotEquals(
            step,
            step.copy(summary = arrayOf(InteractionContent(type = "text", text = "different"))),
        )
        assertEquals(step.hashCode(), step.copy().hashCode())
    }

    @Test
    fun processingStepsRoundTripIdentifiersAndThoughtSummary() {
        val step =
            InteractionStep(
                type = "processing_result",
                id = "step_01",
                callId = "call_01",
                signature = "sig_result_01",
                summary = arrayOf(InteractionContent(type = "text", text = "Loaded transcript")),
            )

        val decoded = json.decodeFromString<InteractionStep>(json.encodeToString(step))

        assertEquals(step, decoded)
    }

    @Test
    fun minimalProcessingStepListPreservesStepDiscriminator() {
        val encoded = """[{"type":"processing_result","id":"step_01"}]"""

        val decoded = json.decodeFromString(InteractionInputSerializer, encoded)

        assertTrue(decoded is InteractionInput.StepList)
        assertEquals("processing_result", decoded.value.single().type)
        assertEquals("step_01", decoded.value.single().id)
    }

    @Test
    fun rawJsonInputKeepsNonStringPrimitive() {
        val encoded = "123"

        val decoded = json.decodeFromString(InteractionInputSerializer, encoded)

        assertEquals(InteractionInput.RawJson(JsonPrimitive(123)), decoded)
    }

    @Test
    fun rawJsonInputSerializesVerbatim() {
        val input = InteractionInput.RawJson(json.decodeFromString<JsonElement>("""{"foo":"bar"}"""))

        val encoded = json.encodeToString(InteractionInputSerializer, input)

        assertEquals(
            json.decodeFromString<JsonElement>("""{"foo":"bar"}"""),
            json.decodeFromString<JsonElement>(encoded),
        )
    }

    @Test
    fun contentListInputRoundTripsAndSerializes() {
        val content = Content(parts = arrayOf(Part(text = "hello")), role = "user")
        val input = InteractionInput.ContentList(arrayOf(content))

        val encoded = json.encodeToString(InteractionInputSerializer, input)
        val decoded = json.decodeFromString(InteractionInputSerializer, encoded)

        assertTrue(encoded.contains("\"parts\""))
        assertTrue(decoded is InteractionInput.ContentList)
        assertEquals(1, decoded.value.size)
        assertEquals("user", decoded.value.first().role)
        assertEquals(
            "hello",
            decoded.value
                .first()
                .parts
                ?.firstOrNull()
                ?.text,
        )
    }

    @Test
    fun stepListInputRoundTrips() {
        val input =
            InteractionInput.StepList(
                arrayOf(
                    InteractionStep(
                        type = "user_input",
                        content = arrayOf(InteractionContent(type = "text", text = "hello")),
                    ),
                ),
            )

        val encoded = json.encodeToString(InteractionInputSerializer, input)
        val decoded = json.decodeFromString(InteractionInputSerializer, encoded)

        assertEquals(input, decoded)
    }

    @Test
    fun turnListInputRoundTrips() {
        val input =
            InteractionInput.TurnList(
                arrayOf(
                    InteractionTurn(
                        role = "user",
                        content =
                            buildJsonObject {
                                put("parts", json.decodeFromString<JsonElement>("""[{"text":"hello"}]"""))
                                put("role", "user")
                            },
                    ),
                ),
            )

        val encoded = json.encodeToString(InteractionInputSerializer, input)
        val decoded = json.decodeFromString(InteractionInputSerializer, encoded)

        assertEquals(input, decoded)
    }

    @Test
    fun emptyArrayDecodesToEmptyContentList() {
        val decoded = json.decodeFromString(InteractionInputSerializer, "[]")

        assertEquals(InteractionInput.ContentList(emptyArray()), decoded)
    }

    @Test
    fun primitiveArrayFallsBackToRawJson() {
        val decoded = json.decodeFromString(InteractionInputSerializer, "[1, 2]")

        assertEquals(json.decodeFromString<JsonElement>("[1, 2]"), (decoded as InteractionInput.RawJson).value)
    }

    @Test
    fun unknownObjectFallsBackToRawJson() {
        val decoded = json.decodeFromString(InteractionInputSerializer, """{"foo":"bar"}""")

        assertEquals(InteractionInput.RawJson(json.decodeFromString<JsonElement>("""{"foo":"bar"}""")), decoded)
    }

    @Test
    fun invalidSingleContentFallsBackToRawJson() {
        val decoded = json.decodeFromString(InteractionInputSerializer, """{"parts":[1]}""")

        assertEquals(InteractionInput.RawJson(json.decodeFromString<JsonElement>("""{"parts":[1]}""")), decoded)
    }

    @Test
    fun invalidStepListFallsBackToRawJson() {
        val decoded =
            json.decodeFromString(
                InteractionInputSerializer,
                """[{"type":"user_input","content":[1]}]""",
            )

        assertEquals(
            InteractionInput.RawJson(
                json.decodeFromString<JsonElement>("""[{"type":"user_input","content":[1]}]"""),
            ),
            decoded,
        )
    }

    @Test
    fun helperFunctionsWrapTypedInputs() {
        val content = Content(parts = arrayOf(Part(text = "hello")), role = "user")
        val steps =
            arrayOf(
                InteractionStep(
                    type = "user_input",
                    content = arrayOf(InteractionContent(type = "text", text = "hello")),
                ),
            )
        val turns =
            arrayOf(
                InteractionTurn(
                    role = "user",
                    content = json.decodeFromString<JsonElement>("""{"parts":[{"text":"hello"}]}"""),
                ),
            )

        assertEquals(InteractionInput.Text("hello"), interactionInput("hello"))
        assertTrue(interactionInput(content) is InteractionInput.SingleContent)
        assertEquals(InteractionInput.StepList(steps), interactionStepsInput(steps))
        assertEquals(InteractionInput.TurnList(turns), interactionTurnsInput(turns))
    }

    @Test
    fun serializerRejectsNonJsonEncoder() {
        assertFailsWith<kotlinx.serialization.SerializationException> {
            InteractionInputSerializer.serialize(NonJsonEncoder, InteractionInput.Text("hello"))
        }
    }

    @Test
    fun serializerRejectsNonJsonDecoder() {
        assertFailsWith<kotlinx.serialization.SerializationException> {
            InteractionInputSerializer.deserialize(NonJsonDecoder)
        }
    }

    private object NonJsonEncoder : AbstractEncoder() {
        override val serializersModule: SerializersModule = SerializersModule {}
    }

    private object NonJsonDecoder : AbstractDecoder() {
        override val serializersModule: SerializersModule = SerializersModule {}

        override fun decodeElementIndex(descriptor: kotlinx.serialization.descriptors.SerialDescriptor): Int = CompositeDecoder.DECODE_DONE
    }
}
