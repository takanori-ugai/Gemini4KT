package io.github.ugaikit.gemini4kt.live

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
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
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Holds the logger.
 */
private val logger = KotlinLogging.logger {}

/**
 * Parse an incoming JSON text into a BidiGenerateContentServerMessage, complete the setup handshake when a `setupComplete` message is seen, and forward the parsed message to the provided channel.
 *
 * @param text Raw JSON text received from the WebSocket.
 * @param handshakeCompleted CompletableDeferred used to signal completion of the initial setup handshake; if the first relevant message contains `setupComplete`, this will be completed, and if a parse error occurs before handshake completion it will be completed exceptionally.
 * @param incomingMessages Channel that receives the decoded BidiGenerateContentServerMessage.
 * @param json Json serializer/deserializer used to decode the incoming text.
 */
private suspend fun processMessage(
    text: String,
    handshakeCompleted: CompletableDeferred<Unit>,
    incomingMessages: Channel<BidiGenerateContentServerMessage>,
    json: Json,
) {
    try {
        val message = json.decodeFromString<BidiGenerateContentServerMessage>(text)

        // Check for handshake completion on the first relevant message
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

/**
 * A client for interacting with the Gemini Live API via WebSockets.
 */
class GeminiLive(
    private val apiKey: String,
    private val model: String,
    private val config: LiveConnectConfig? = null,
    /**
     * Holds the json.
     */
    private val json: Json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        },
    private val client: HttpClient? = null,
) {
    // Base URL for WebSocket connection.

    /**
     * Holds the ws url.
     */
    private val wsUrl =
        "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"

    /**
     * Opens a WebSocket connection to the Gemini Live API, sends the initial setup message, and establishes a session for ongoing bidirectional communication.
     *
     * If `setup` is null, a setup message is constructed from the instance `model` and `config` (including generationConfig, systemInstruction, and tools) and sent instead.
     *
     * @param setup Optional explicit setup message to send as the initial client payload; when omitted, a setup is derived from the instance configuration.
     * @return A GeminiLiveSession representing the established WebSocket session, the incoming message channel, the JSON serializer, and the listener job.
     */
    suspend fun connect(setup: BidiGenerateContentSetup? = null): GeminiLiveSession {
        // Use provided client or create a new one.
        val httpClient =
            client?.config {
                install(WebSockets)
            } ?: HttpClient {
                install(WebSockets)
            }

        val urlString = wsUrl

        logger.info { "Connecting to WebSocket at $urlString" }

        var session: DefaultClientWebSocketSession? = null
        try {
            session =
                httpClient.webSocketSession(urlString) {
                    header("x-goog-api-key", apiKey)
                    header("x-goog-api-client", "gemini4kt")
                }

            val incomingMessages = Channel<BidiGenerateContentServerMessage>(Channel.UNLIMITED)
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
                                processMessage(text, handshakeCompleted, incomingMessages, json)
                            } else if (frame is Frame.Binary) {
                                val bytes = frame.data
                                val text = bytes.decodeToString()
                                logger.debug { "Received binary frame with size: ${bytes.size}" }
                                logger.debug { "Binary frame content as string: $text" }
                                processMessage(text, handshakeCompleted, incomingMessages, json)
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
            val setupMessage =
                setup ?: run {
                    val generationConfig =
                        when {
                            config?.generationConfig != null ->
                                config.generationConfig.copy(
                                    responseModalities =
                                        config.responseModalities
                                            ?: config.generationConfig.responseModalities,
                                    speechConfig = config.speechConfig ?: config.generationConfig.speechConfig,
                                )
                            config?.responseModalities != null || config?.speechConfig != null -> {
                                val connectConfig = requireNotNull(config)
                                io.github.ugaikit.gemini4kt.GenerationConfig(
                                    responseModalities = connectConfig.responseModalities,
                                    speechConfig = connectConfig.speechConfig,
                                )
                            }
                            else -> null
                        }

                    // Ensure model has "models/" prefix if not present
                    val modelName = if (model.startsWith("models/")) model else "models/$model"

                    BidiGenerateContentSetup(
                        model = modelName,
                        generationConfig = generationConfig,
                        systemInstruction = config?.systemInstruction,
                        tools = config?.tools,
                    )
                }

            val clientMessage = BidiGenerateContentClientMessage(setup = setupMessage)
            val jsonMessage = json.encodeToString(clientMessage)
            logger.debug { "Sending setup message: $jsonMessage" }
            session.send(Frame.Text(jsonMessage))

            // Wait for setup complete message
            try {
                withTimeout(10_000) {
                    handshakeCompleted.await()
                }
            } catch (e: Exception) {
                logger.error(e) { "Error waiting for SetupComplete" }
                // Close resources
                listenerJob.cancel()
                session.close()
                throw e
            }

            return GeminiLiveSession(session, incomingMessages, json, listenerJob)
        } catch (e: Exception) {
            session?.close()
            throw e
        }
    }
}

/**
 * Represents the gemini live session.
 *
 * @property session The session.
 * @property incomingMessages The incoming messages.
 * @property json The json.
 * @property listenerJob The listener job.
 */
class GeminiLiveSession(
    private val session: WebSocketSession,
    private val incomingMessages: Channel<BidiGenerateContentServerMessage>,
    private val json: Json,
    private val listenerJob: Job,
) {
    /**
     * Sends a client content message.
     */
    suspend fun sendClientContent(content: BidiGenerateContentClientContent) {
        val msg = BidiGenerateContentClientMessage(clientContent = content)
        send(msg)
    }

    /**
     * Sends a realtime input message.
     */
    suspend fun sendRealtimeInput(input: BidiGenerateContentRealtimeInput) {
        val msg = BidiGenerateContentClientMessage(realtimeInput = input)
        send(msg)
    }

    /**
     * Sends a tool response message.
     */
    suspend fun sendToolResponse(response: BidiGenerateContentToolResponse) {
        val msg = BidiGenerateContentClientMessage(toolResponse = response)
        send(msg)
    }

    /**
     * Handles send.
     *
     * @param msg The msg.
     */
    private suspend fun send(msg: BidiGenerateContentClientMessage) {
        val txt = json.encodeToString(msg)
        logger.debug { "Sending message: $txt" }
        session.send(Frame.Text(txt))
    }

    /**
     * Receives messages from the server.
     */
    fun receive(): Flow<BidiGenerateContentServerMessage> = incomingMessages.receiveAsFlow()

    /**
     * Closes the session.
     */
    suspend fun close() {
        session.close()
        listenerJob.cancel()
        incomingMessages.close()
    }
}
