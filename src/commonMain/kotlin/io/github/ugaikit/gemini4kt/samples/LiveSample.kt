package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.Modality
import io.github.ugaikit.gemini4kt.content
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.getLiveClient
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentRealtimeInput
import io.github.ugaikit.gemini4kt.live.Blob
import io.github.ugaikit.gemini4kt.live.LiveConnectConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Represents the live sample.
 */
@OptIn(ExperimentalEncodingApi::class)
object LiveSample {
    /**
     * Runs the Live Sample.
     *
     * @param inputAudioBase64 Base64 encoded input audio data (e.g. PCM 16kHz). If null, no audio input is sent.
     * @param onAudioData Callback to handle received audio data (Base64 encoded string).
     */
    suspend fun run(
        inputAudioBase64: String?,
        onAudioData: (String) -> Unit,
        gemini: Gemini? = null,
    ) {
        val apiKey = getApiKey()

        val liveModel = "gemini-2.5-flash-native-audio-preview-12-2025"
        val config =
            LiveConnectConfig(
                responseModalities = arrayOf(Modality.AUDIO),
                systemInstruction =
                    content {
                        part {
                            text { "You are a helpful assistant and answer in a friendly tone." }
                        }
                    },
            )

        val client = gemini ?: Gemini(apiKey)
        val liveClient = client.getLiveClient(liveModel, config)

        try {
            val session = liveClient.connect()

            if (inputAudioBase64 != null) {
                session.sendRealtimeInput(
                    BidiGenerateContentRealtimeInput(
                        audio = Blob(data = inputAudioBase64, mimeType = "audio/pcm;rate=16000"),
                    ),
                )
            } else {
                println("No input audio provided.")
            }

            try {
                session.receive().collect { msg ->
                    if (msg.serverContent?.turnComplete == true) {
                        throw CancellationException("Turn complete")
                    }
                    println(msg)
                    msg.serverContent?.modelTurn?.parts?.forEach { part ->
                        part.inlineData?.let {
                            if (it.mimeType.startsWith("audio")) {
                                onAudioData(it.data)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                println("Turn completed.")
            }

            session.close()
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            println("Error in LiveSample: ${e.message}")
            throw e
        }
    }
}
