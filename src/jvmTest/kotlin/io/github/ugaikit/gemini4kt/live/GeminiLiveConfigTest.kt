package io.github.ugaikit.gemini4kt.live

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests internal GeminiLive configuration values via reflection.
 */
class GeminiLiveConfigTest {
    /**
     * Ensures the websocket URL matches the documented endpoint.
     */
    @Test
    fun wsUrlMatchesExpectedEndpoint() {
        val instance = GeminiLive(apiKey = "key", model = "gemini-live")
        val wsUrlProperty =
            GeminiLive::class.declaredMemberProperties.first { it.name == "wsUrl" }.apply {
                isAccessible = true
            }
        val wsUrl = wsUrlProperty.get(instance) as String

        assertEquals(
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage." +
                "v1beta.GenerativeService.BidiGenerateContent",
            wsUrl,
        )
    }

    /**
     * Confirms default Json settings align with expectations.
     */
    @Test
    fun defaultJsonConfigurationEnablesEncodeDefaults() {
        val instance = GeminiLive(apiKey = "key", model = "gemini-live")
        val jsonProperty =
            GeminiLive::class.declaredMemberProperties.first { it.name == "json" }.apply {
                isAccessible = true
            }
        val json = jsonProperty.get(instance) as Json

        assertTrue(json.configuration.encodeDefaults)
        assertTrue(json.configuration.ignoreUnknownKeys)
    }

    @Test
    fun connectSendsVoiceEditingConfigurationInSetup() {
        val transcription = AudioTranscriptionConfig(AudioTranscriptionMode.SMART, "Original")
        val realtime = RealtimeInputConfig(automaticActivityDetection = AutomaticActivityDetection(disabled = true))
        val setup = sentSetup(LiveConnectConfig(inputAudioTranscription = transcription, realtimeInputConfig = realtime))
        assertEquals(transcription, setup.inputAudioTranscription)
        assertEquals(realtime, setup.realtimeInputConfig)
    }

    @Test
    fun connectLeavesVoiceEditingSettingsUnsetByDefault() {
        val setup = sentSetup(null)
        assertEquals(null, setup.inputAudioTranscription)
        assertEquals(null, setup.realtimeInputConfig)
    }

    private fun sentSetup(config: LiveConnectConfig?): BidiGenerateContentSetup =
        runBlocking {
            withTimeout(5_000) {
                val socket = MockWebSocketSession()
                val engine = MockEngine {
                    HttpResponseData(
                        HttpStatusCode.SwitchingProtocols,
                        GMTDate(),
                        Headers.Empty,
                        HttpProtocolVersion.HTTP_1_1,
                        socket,
                        currentCoroutineContext(),
                    )
                }
                val client = HttpClient(engine)
                val live = GeminiLive("key", "gemini-3.5-transcribe-live", config, client = client)
                val handshake = launch {
                    socket.incoming.send(Frame.Text("""{"setupComplete":{}}"""))
                }
                try {
                    val session = live.connect()
                    try {
                        val frame = socket.outgoing.receive() as Frame.Text
                        requireNotNull(Json.decodeFromString<BidiGenerateContentClientMessage>(frame.readText()).setup)
                    } finally {
                        session.close()
                    }
                } finally {
                    handshake.cancel()
                    socket.cancel()
                    client.close()
                    engine.close()
                }
            }
        }
}
