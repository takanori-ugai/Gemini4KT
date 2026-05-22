package io.github.ugaikit.gemini4kt.live

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.GenerationConfig
import io.github.ugaikit.gemini4kt.Part
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Represents the live types test.
 */
class LiveTypesTest {
    /**
     * Holds the json.
     */
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    /**
     * Tests test bidi generate content setup serialization.
     */
    @Test
    fun testBidiGenerateContentSetupSerialization() {
        val setup =
            BidiGenerateContentSetup(
                model = "models/gemini-2.0-flash-exp",
                generationConfig = GenerationConfig(temperature = 0.5),
                systemInstruction = Content(parts = arrayOf(Part(text = "Hello"))),
            )
        val jsonStr = json.encodeToString(setup)
        assertNotNull(jsonStr)
        // Check for key fields
        assertTrue(jsonStr.contains("models/gemini-2.0-flash-exp"))
        assertTrue(jsonStr.contains("generationConfig"))
        assertTrue(jsonStr.contains("systemInstruction"))
    }

    /**
     * Tests test bidi generate content client message serialization.
     */
    @Test
    fun testBidiGenerateContentClientMessageSerialization() {
        val setup = BidiGenerateContentSetup(model = "models/gemini-pro")
        val msg = BidiGenerateContentClientMessage(setup = setup)
        val jsonStr = json.encodeToString(msg)
        assertNotNull(jsonStr)
        assertTrue(jsonStr.contains("setup"))

        val content =
            BidiGenerateContentClientContent(
                turns = listOf(Content(parts = arrayOf(Part(text = "Hi")))),
                turnComplete = true,
            )
        val msg2 = BidiGenerateContentClientMessage(clientContent = content)
        val jsonStr2 = json.encodeToString(msg2)
        assertTrue(jsonStr2.contains("clientContent"))
        assertTrue(jsonStr2.contains("turnComplete"))
    }

    /**
     * Tests test bidi generate content server message deserialization.
     */
    @Test
    fun testBidiGenerateContentServerMessageDeserialization() {
        val jsonStr =
            """
            {
                "serverContent": {
                    "modelTurn": {
                        "parts": [{"text": "Hello there"}]
                    },
                    "turnComplete": true
                }
            }
            """.trimIndent()

        val msg = json.decodeFromString<BidiGenerateContentServerMessage>(jsonStr)
        assertNotNull(msg.serverContent)
        val serverContent = checkNotNull(msg.serverContent)
        assertEquals(true, serverContent.turnComplete)
        assertEquals(
            "Hello there",
            serverContent
                .modelTurn
                ?.parts
                ?.first()
                ?.text,
        )
    }

    /**
     * Tests test realtime input serialization.
     */
    @Test
    fun testRealtimeInputSerialization() {
        val input =
            BidiGenerateContentRealtimeInput(
                mediaChunks = listOf(Blob(mimeType = "audio/pcm", data = "base64encodeddata")),
                audio = Blob(mimeType = "audio/wav", data = "somesound"),
                text = "Some text input",
            )
        val msg = BidiGenerateContentClientMessage(realtimeInput = input)
        val jsonStr = json.encodeToString(msg)

        assertTrue(jsonStr.contains("realtimeInput"))
        assertTrue(jsonStr.contains("mediaChunks"))
        assertTrue(jsonStr.contains("base64encodeddata"))
        assertTrue(jsonStr.contains("text"))
        assertTrue(jsonStr.contains("Some text input"))
    }

    /**
     * Tests test bidi generate content tool response serialization.
     */
    @Test
    fun testBidiGenerateContentToolResponseSerialization() {
        val toolResponse =
            BidiGenerateContentToolResponse(
                functionResponses = listOf(),
            )
        val msg = BidiGenerateContentClientMessage(toolResponse = toolResponse)
        val jsonStr = json.encodeToString(msg)
        assertTrue(jsonStr.contains("toolResponse"))
        assertTrue(jsonStr.contains("functionResponses"))
    }

    /**
     * Tests test bidi generate content tool call deserialization.
     */
    @Test
    fun testBidiGenerateContentToolCallDeserialization() {
        val jsonStr =
            """
            {
                "toolCall": {
                    "functionCalls": [
                        { "name": "get_weather", "args": {"location": "London"} }
                    ]
                }
            }
            """.trimIndent()
        val msg = json.decodeFromString<BidiGenerateContentServerMessage>(jsonStr)
        assertNotNull(msg.toolCall)
        val calls = checkNotNull(checkNotNull(msg.toolCall).functionCalls)
        assertEquals(1, calls.size)
        assertEquals("get_weather", calls[0].name)
    }

    @Test
    fun testServerMessageDeserializesTranscriptionAndControlFields() {
        val raw =
            """
            {
              "toolCallCancellation": {"ids": ["call-1"]},
              "goAway": {"timeLeft": "10s"},
              "sessionResumptionUpdate": {"newHandle": "h1", "resumable": true},
              "serverContent": {
                "inputTranscription": {"text": "input"},
                "outputTranscription": {"text": "output"}
              }
            }
            """.trimIndent()

        val decoded = json.decodeFromString<BidiGenerateContentServerMessage>(raw)
        assertEquals(listOf("call-1"), decoded.toolCallCancellation?.ids)
        assertEquals("10s", decoded.goAway?.timeLeft)
        assertEquals("h1", decoded.sessionResumptionUpdate?.newHandle)
        assertEquals(true, decoded.sessionResumptionUpdate?.resumable)
        assertEquals("input", decoded.serverContent?.inputTranscription?.text)
        assertEquals("output", decoded.serverContent?.outputTranscription?.text)
    }

    @Test
    fun testSetupSerializesResumptionCompressionAndProactivity() {
        val setup =
            BidiGenerateContentSetup(
                model = "models/gemini-2.5-flash",
                sessionResumption = SessionResumptionConfig(handle = "resume-1"),
                contextWindowCompression =
                    ContextWindowCompressionConfig(
                        slidingWindow = SlidingWindow(targetTokens = 512L),
                        triggerTokens = 1024L,
                    ),
                proactivity = ProactivityConfig(proactiveAudio = true),
            )

        val encoded = json.encodeToString(setup)
        assertTrue(encoded.contains("\"sessionResumption\""))
        assertTrue(encoded.contains("\"contextWindowCompression\""))
        assertTrue(encoded.contains("\"targetTokens\":512"))
        assertTrue(encoded.contains("\"proactivity\""))
    }
}
