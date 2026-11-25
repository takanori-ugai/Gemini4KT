package io.github.ugaikit.gemini4kt.live

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
    fun defaultSetupCarriesVoiceEditingConfiguration() {
        val transcription = AudioTranscriptionConfig(AudioTranscriptionMode.SMART, "Original")
        val realtime = RealtimeInputConfig(automaticActivityDetection = AutomaticActivityDetection(disabled = true))
        val live =
            GeminiLive(
                apiKey = "key",
                model = "gemini-3.5-transcribe-live",
                config = LiveConnectConfig(inputAudioTranscription = transcription, realtimeInputConfig = realtime),
            )

        val setup = live.defaultSetup()
        assertEquals(transcription, setup.inputAudioTranscription)
        assertEquals(realtime, setup.realtimeInputConfig)
    }

    @Test
    fun defaultSetupLeavesVoiceEditingSettingsUnsetByDefault() {
        val setup = GeminiLive(apiKey = "key", model = "gemini-live").defaultSetup()
        assertEquals(null, setup.inputAudioTranscription)
        assertEquals(null, setup.realtimeInputConfig)
    }
}
