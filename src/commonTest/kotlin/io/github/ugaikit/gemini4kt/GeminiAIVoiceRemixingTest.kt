package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.InteractionAudioContent
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class GeminiAIVoiceRemixingTest {
    private val clients = TestHttpClientTracker()

    @AfterTest
    fun closeClients() {
        clients.closeAll()
    }

    private fun createClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): GeminiAI {
        val client =
            HttpClient(MockEngine) {
                engine { addHandler(handler) }
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
        return GeminiAI(client = clients.track(client), apiKey = "test-key")
    }

    @Test
    fun remixCreatesIndependentVoicesAndPreservesLineageAndPreview() =
        runTest {
            for (source in listOf("Puck", "voice_designed")) {
                val ai =
                    createClient { request ->
                        assertEquals(HttpMethod.Post, request.method)
                        assertEquals("/v1beta/voices", request.url.encodedPath)
                        assertEquals("test-key", request.headers["x-goog-api-key"])
                        val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                        assertEquals(JsonPrimitive(true), body["store"])
                        val voice = body.getValue("voice").jsonObject
                        assertEquals(JsonPrimitive("gemini-3.8-flash-tts"), voice["model"])
                        assertEquals(JsonPrimitive("prompted"), voice["type"])
                        assertEquals(JsonPrimitive("Narrator"), voice["display_name"])
                        val prompted = voice.getValue("prompted").jsonObject
                        assertEquals(JsonPrimitive(source), prompted["base_voice"])
                        assertEquals(JsonPrimitive("Speak more softly."), prompted["input"])
                        respond(
                            """
                            {"id":"voice_remixed","type":"prompted",
                             "prompted":{"base_voice":"$source","input":"Speak more softly."},
                             "expire_time":"2027-11-16T00:00:00Z",
                             "sample_audio":{"data":"AQID","mime_type":"audio/wav"}}
                            """.trimIndent(),
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                val voice = ai.createRemixedVoice(source, "Speak more softly.", "Narrator")
                assertNotEquals(source, voice.id)
                assertEquals(source, voice.prompted?.baseVoice)
                assertEquals("Speak more softly.", voice.prompted?.input)
                assertEquals("2027-11-16T00:00:00Z", voice.expireTime)
                assertEquals("AQID", voice.sampleAudio?.data)
                assertEquals("audio/wav", voice.sampleAudio?.mimeType)
            }
        }

    @Test
    fun remixOmitsAnUnspecifiedDisplayName() =
        runTest {
            val ai =
                createClient { request ->
                    val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                    assertFalse(body.getValue("voice").jsonObject.containsKey("display_name"))
                    respond(
                        """{"id":"voice_remixed"}""",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            assertEquals("voice_remixed", ai.createRemixedVoice("Puck", "Lower the pitch.").id)
        }

    @Test
    fun remixedVoiceIdsCanBeUsedWithBothTtsModels() =
        runTest {
            val models = listOf("gemini-3.8-flash-tts", "gemini-3.8-flash-lite-tts")
            val requestedModels = mutableListOf<String>()
            val ai =
                createClient { request ->
                    val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                    requestedModels.add(body.getValue("model").jsonPrimitive.content)
                    val speech =
                        body
                            .getValue("generation_config")
                            .jsonObject
                            .getValue("speech_config")
                            .jsonArray
                    assertEquals(JsonPrimitive("voice_remixed"), speech.single().jsonObject["voice"])
                    assertEquals(JsonObject(mapOf("type" to JsonPrimitive("audio"))), body["response_format"])
                    respond(
                        """{"id":"tts_1","status":"completed","output_audio":{"data":"AQID"}}""",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            for (model in models) {
                val speech = ai.generateSpeech("Hello", voice = "voice_remixed", model = model)
                assertEquals("AQID", speech.outputAudio?.data)
            }
            assertEquals(models, requestedModels)
        }

    @Test
    fun getAndListVoicesPreserveRemixMetadata() =
        runTest {
            val expected =
                Voice(
                    id = "voice_remixed",
                    displayName = "Narrator",
                    type = "prompted",
                    model = "gemini-3.8-flash-tts",
                    gender = "female",
                    languageCode = "en-US",
                    accent = "American",
                    pitch = "low",
                    persona = "calm",
                    description = "A calm narrator",
                    sampleAudio = InteractionAudioContent(data = "AQID", mimeType = "audio/wav"),
                    prompted = PromptedVoiceConfig(input = "Speak more softly.", baseVoice = "Puck"),
                    expireTime = "2027-11-16T00:00:00Z",
                )
            val voiceJson = Json.encodeToString(expected)
            val ai =
                createClient { request ->
                    val body =
                        if (request.url.encodedPath.endsWith("/voice_remixed")) {
                            voiceJson
                        } else {
                            """{"voices":[$voiceJson]}"""
                        }
                    respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
                }
            assertEquals(expected, ai.getVoice("voice_remixed"))
            assertEquals(expected, ai.listVoices().voices?.single())
        }

    @Test
    fun remixRejectsBlankInputsBeforeSendingRequests() =
        runTest {
            val ai = createClient { error("Invalid inputs must not make HTTP requests.") }
            assertFailsWith<IllegalArgumentException> { ai.createRemixedVoice(" ", "Softer") }
            assertFailsWith<IllegalArgumentException> { ai.createRemixedVoice("Puck", " ") }
            assertFailsWith<IllegalArgumentException> { ai.createRemixedVoice("Puck", "Softer", displayName = " ") }
            assertFailsWith<IllegalArgumentException> { ai.createRemixedVoice("Puck", "Softer", model = " ") }
        }

    @Test
    fun remixPreservesApiErrors() =
        runTest {
            val ai =
                createClient {
                    respond(
                        """{"error":{"code":404,"message":"Unknown voice","status":"NOT_FOUND"}}""",
                        HttpStatusCode.NotFound,
                        headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val error = assertFailsWith<GeminiException> { ai.createRemixedVoice("voice_missing", "Softer") }
            assertEquals(404, error.error.code)
            assertEquals("Unknown voice", error.error.message)
        }
}
