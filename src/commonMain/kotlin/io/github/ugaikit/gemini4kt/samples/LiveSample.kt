package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.Modality
import io.github.ugaikit.gemini4kt.content
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.getLiveClient
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentClientContent
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentRealtimeInput
import io.github.ugaikit.gemini4kt.live.BidiGenerateContentServerMessage
import io.github.ugaikit.gemini4kt.live.Blob
import io.github.ugaikit.gemini4kt.live.LiveConnectConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Represents the live sample.
 */
@OptIn(ExperimentalEncodingApi::class)
object LiveSample {
    interface Session {
        fun receive(): kotlinx.coroutines.flow.Flow<BidiGenerateContentServerMessage>

        suspend fun sendRealtimeInput(input: BidiGenerateContentRealtimeInput)

        suspend fun sendClientContent(content: BidiGenerateContentClientContent)

        suspend fun close()
    }

    interface LiveClient {
        suspend fun connect(): Session
    }

    /**
     * Executes an interactive live audio session with the Gemini live model.
     *
     * Streams optional Base64-encoded input audio to the model, delivers server-produced inline
     * audio parts to `onAudioData`, waits up to 30 seconds for the model turn to complete, and
     * closes the session.
     *
     * @param inputAudioBase64 Base64-encoded input audio (e.g., PCM 16kHz). If `null`, no input audio is sent.
     * @param onAudioData Callback invoked with Base64-encoded audio data received from the server.
     * @param gemini Optional Gemini client instance; if `null`, a new client is created using the configured API key.
     */
    suspend fun run(
        inputAudioBase64: String?,
        onAudioData: (String) -> Unit,
        gemini: Gemini? = null,
        clientFactory: (() -> LiveClient)? = null,
    ): Unit =
        coroutineScope {
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

            val liveClient: LiveClient =
                clientFactory?.invoke()
                    ?: run {
                        val geminiClient = gemini ?: Gemini(getApiKey())
                        geminiClient.getLiveClient(liveModel, config).toLiveClient()
                    }

            try {
                val session = liveClient.connect()
                try {
                    val turnCompleted = CompletableDeferred<Unit>()

                    val receiveJob =
                        launch {
                            session.receive().collect { msg ->
                                println(msg)
                                msg.serverContent?.modelTurn?.parts?.forEach { part ->
                                    part.inlineData?.let {
                                        if (it.mimeType.startsWith("audio")) {
                                            onAudioData(it.data)
                                        }
                                    }
                                }
                                if (msg.serverContent?.turnComplete == true) {
                                    println("Turn complete")
                                    turnCompleted.complete(Unit)
                                }
                            }
                        }

                    val audioProvided =
                        if (inputAudioBase64 != null) {
                            session.sendRealtimeInput(
                                BidiGenerateContentRealtimeInput(
                                    media =
                                        Blob(
                                            data = inputAudioBase64,
                                            mimeType = "audio/pcm;rate=16000",
                                        ),
                                ),
                            )
                            true
                        } else {
                            println("No input audio provided.")
                            false
                        }

                    // Let the server know audio streaming is finished, then close the turn so it can respond.
                    if (audioProvided) {
                        session.sendRealtimeInput(BidiGenerateContentRealtimeInput(audioStreamEnd = true))
                    }
                    session.sendClientContent(
                        BidiGenerateContentClientContent(
                            turns =
                                listOf(
                                    content {
                                        role = "user"
                                        part {
                                            text {
                                                if (audioProvided) {
                                                    "Audio provided; please respond."
                                                } else {
                                                    "Hello! Please respond."
                                                }
                                            }
                                        }
                                    },
                                ),
                            turnComplete = true,
                        ),
                    )

                    val completed =
                        withTimeoutOrNull(30_000) {
                            turnCompleted.await()
                        }
                    if (completed == null) {
                        println("Timed out waiting for server turnComplete; closing session.")
                    }
                    receiveJob.cancelAndJoin()
                } catch (e: CancellationException) {
                    println("LiveSample cancelled: ${e.message}")
                    throw e
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    println("Error in LiveSample: ${e.message}")
                    throw e
                } finally {
                    session.close()
                }
            } catch (e: CancellationException) {
                println("LiveSample cancelled: ${e.message}")
                throw e
            }
        }
}

private fun io.github.ugaikit.gemini4kt.live.GeminiLive.toLiveClient(): LiveSample.LiveClient =
    object : LiveSample.LiveClient {
        override suspend fun connect(): LiveSample.Session {
            val session = this@toLiveClient.connect()
            return object : LiveSample.Session {
                override fun receive(): kotlinx.coroutines.flow.Flow<BidiGenerateContentServerMessage> = session.receive()

                override suspend fun sendRealtimeInput(input: BidiGenerateContentRealtimeInput) = session.sendRealtimeInput(input)

                override suspend fun sendClientContent(content: BidiGenerateContentClientContent) = session.sendClientContent(content)

                override suspend fun close() = session.close()
            }
        }
    }
