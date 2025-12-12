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

class LiveTypesTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    @Test
    fun `test BidiGenerateContentSetup serialization`() {
        val setup =
            BidiGenerateContentSetup(
                model = "models/gemini-2.0-flash-exp",
                generationConfig = GenerationConfig(temperature = 0.5),
                systemInstruction = Content(parts = listOf(Part(text = "Hello"))),
            )
        val jsonStr = json.encodeToString(setup)
        assertNotNull(jsonStr)
        // Check for key fields
        assertTrue(jsonStr.contains("models/gemini-2.0-flash-exp"))
        assertTrue(jsonStr.contains("generationConfig"))
        assertTrue(jsonStr.contains("systemInstruction"))
    }

    @Test
    fun `test BidiGenerateContentClientMessage serialization`() {
        val setup = BidiGenerateContentSetup(model = "models/gemini-pro")
        val msg = BidiGenerateContentClientMessage(setup = setup)
        val jsonStr = json.encodeToString(msg)
        assertNotNull(jsonStr)
        assertTrue(jsonStr.contains("setup"))

        val content =
            BidiGenerateContentClientContent(
                turns = listOf(Content(parts = listOf(Part(text = "Hi")))),
                turnComplete = true,
            )
        val msg2 = BidiGenerateContentClientMessage(clientContent = content)
        val jsonStr2 = json.encodeToString(msg2)
        assertTrue(jsonStr2.contains("clientContent"))
        assertTrue(jsonStr2.contains("turnComplete"))
    }

    @Test
    fun `test BidiGenerateContentServerMessage deserialization`() {
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
        assertEquals(true, msg.serverContent?.turnComplete)
        assertEquals(
            "Hello there",
            msg.serverContent
                ?.modelTurn
                ?.parts
                ?.first()
                ?.text,
        )
    }

    @Test
    fun `test RealtimeInput serialization`() {
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
}
