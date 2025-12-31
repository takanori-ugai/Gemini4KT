package io.github.ugaikit.gemini4kt.live.music

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

/**
 * A client for interacting with the Gemini Live Music API via WebSockets.
 */
class LiveMusic(
    private val apiKey: String,
    private val model: String,
    private val json: Json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        },
    private val client: HttpClient? = null,
) {
    private val wsUrl =
        "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateMusic"

    /**
     * Connects to the Live Music API and sends the initial setup message.
     */
    suspend fun connect(): LiveMusicSession {
        // Use provided client or create a new one.
        val httpClient =
            client?.config {
                install(WebSockets)
            } ?: HttpClient {
                install(WebSockets)
            }

        val urlString = "$wsUrl?key=$apiKey"

        logger.info { "Connecting to WebSocket at $urlString" }

        var session: DefaultClientWebSocketSession? = null
        try {
            session = httpClient.webSocketSession(urlString)

            val incomingMessages = Channel<LiveMusicServerMessage>(Channel.UNLIMITED)
            val scope = CoroutineScope(Dispatchers.Default)
            val handshakeCompleted = CompletableDeferred<Unit>()

            // Launch a coroutine to listen for messages
            val listenerJob =
                scope.launch {
                    try {
                        for (frame in session.incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                logger.debug { "Received message: $text" }
                                try {
                                    val message = json.decodeFromString<LiveMusicServerMessage>(text)

                                    if (!handshakeCompleted.isCompleted) {
                                        if (message.setupComplete != null) {
                                            handshakeCompleted.complete(Unit)
                                        } else {
                                            logger.warn { "Received message before SetupComplete: $message" }
                                        }
                                    }

                                    incomingMessages.send(message)
                                } catch (e: Exception) {
                                    logger.error(e) { "Failed to parse message" }
                                    if (!handshakeCompleted.isCompleted) {
                                        handshakeCompleted.completeExceptionally(e)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "WebSocket error" }
                        incomingMessages.close(e)
                        if (!handshakeCompleted.isCompleted) {
                            handshakeCompleted.completeExceptionally(e)
                        }
                    } finally {
                        incomingMessages.close()
                    }
                }

            // Send Setup Message
            // Ensure model has "models/" prefix if not present
            val modelName = if (model.startsWith("models/")) model else "models/$model"
            val setup = LiveMusicClientSetup(model = modelName)

            val clientMessage = LiveMusicClientMessage(setup = setup)
            val jsonMessage = json.encodeToString(clientMessage)
            logger.debug { "Sending setup message: $jsonMessage" }
            session.send(Frame.Text(jsonMessage))

            // Wait for setup complete message
            try {
                handshakeCompleted.await()
            } catch (e: Exception) {
                logger.error(e) { "Error waiting for SetupComplete" }
                // Close resources
                listenerJob.cancel()
                session.close()
                throw e
            }

            return LiveMusicSession(session, incomingMessages, json, listenerJob)
        } catch (e: Exception) {
            session?.close()
            throw e
        }
    }
}

/**
 * Represents the live music session.
 */
class LiveMusicSession(
    private val session: WebSocketSession,
    private val incomingMessages: Channel<LiveMusicServerMessage>,
    private val json: Json,
    private val listenerJob: Job,
) {
    /**
     * Sets inputs to steer music generation. Updates the session's current weighted prompts.
     */
    suspend fun setWeightedPrompts(weightedPrompts: List<WeightedPrompt>) {
        if (weightedPrompts.isEmpty()) {
            throw IllegalArgumentException("Weighted prompts must contain at least one entry.")
        }
        val clientContent = LiveMusicClientContent(weightedPrompts = weightedPrompts)
        val msg = LiveMusicClientMessage(clientContent = clientContent)
        send(msg)
    }

    /**
     * Sets a configuration to the model. Updates the session's current music generation config.
     */
    suspend fun setMusicGenerationConfig(config: LiveMusicGenerationConfig) {
        val msg = LiveMusicClientMessage(musicGenerationConfig = config)
        send(msg)
    }

    private suspend fun sendPlaybackControl(control: LiveMusicPlaybackControl) {
        val msg = LiveMusicClientMessage(playbackControl = control)
        send(msg)
    }

    /**
     * Start the music stream.
     */
    suspend fun play() {
        sendPlaybackControl(LiveMusicPlaybackControl.PLAY)
    }

    /**
     * Temporarily halt the music stream. Use `play` to resume from the current position.
     */
    suspend fun pause() {
        sendPlaybackControl(LiveMusicPlaybackControl.PAUSE)
    }

    /**
     * Stop the music stream and reset the state. Retains the current prompts and config.
     */
    suspend fun stop() {
        sendPlaybackControl(LiveMusicPlaybackControl.STOP)
    }

    /**
     * Resets the context of the music generation without stopping it.
     * Retains the current prompts and config.
     */
    suspend fun resetContext() {
        sendPlaybackControl(LiveMusicPlaybackControl.RESET_CONTEXT)
    }

    /**
     * Handles send.
     */
    private suspend fun send(msg: LiveMusicClientMessage) {
        val txt = json.encodeToString(msg)
        logger.debug { "Sending message: $txt" }
        session.send(Frame.Text(txt))
    }

    /**
     * Receives messages from the server.
     */
    fun receive(): Flow<LiveMusicServerMessage> = incomingMessages.receiveAsFlow()

    /**
     * Closes the session.
     */
    suspend fun close() {
        session.close()
        listenerJob.cancel()
        incomingMessages.close()
    }
}
