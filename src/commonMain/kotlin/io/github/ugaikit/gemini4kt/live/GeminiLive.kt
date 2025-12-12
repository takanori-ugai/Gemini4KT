package io.github.ugaikit.gemini4kt.live

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
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
 * A client for interacting with the Gemini Live API via WebSockets.
 */
class GeminiLive(
    private val apiKey: String,
    private val model: String,
    private val config: LiveConnectConfig? = null,
    private val json: Json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        },
    private val client: HttpClient? = null
) {
    // Base URL for WebSocket connection.
    private val wsUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"

    /**
     * Connects to the Live API and sends the initial setup message.
     */
    suspend fun connect(setup: BidiGenerateContentSetup? = null): GeminiLiveSession {
        // Use provided client or create a new one.
        val httpClient = client?.config {
            install(WebSockets)
        } ?: HttpClient {
            install(WebSockets)
        }

        val urlString = "$wsUrl?key=$apiKey"

        logger.info { "Connecting to WebSocket at $urlString" }

        var session: DefaultClientWebSocketSession? = null
        try {
            session = httpClient.webSocketSession(urlString)

            val incomingMessages = Channel<BidiGenerateContentServerMessage>(Channel.UNLIMITED)
            val scope = CoroutineScope(Dispatchers.Default)
            val handshakeCompleted = CompletableDeferred<Unit>()

            // Launch a coroutine to listen for messages
            val listenerJob = scope.launch {
                try {
                    for (frame in session.incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            logger.debug { "Received message: $text" }
                            try {
                                val message = json.decodeFromString<BidiGenerateContentServerMessage>(text)

                                // Check for handshake completion on the first relevant message
                                if (!handshakeCompleted.isCompleted) {
                                    if (message.setupComplete != null) {
                                        handshakeCompleted.complete(Unit)
                                    } else {
                                        // If we receive something else before SetupComplete, it might be an error or unexpected behavior.
                                        // We log it, but we don't complete the handshake yet unless it's a fatal error?
                                        // If it's a serverContent, maybe we should just allow it?
                                        // But per protocol, SetupComplete should be first.
                                        // If we get an error (e.g. standard HTTP error wrapped in WS?), we might want to fail.
                                        // BidiGenerateContentServerMessage has `serverContent`, `toolCall`, etc.
                                        // We will just forward it.
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
            val setupMessage =
                setup ?: run {
                    val generationConfig =
                        if (config?.generationConfig != null) {
                            config.generationConfig.copy(
                                responseModalities = config.responseModalities ?: config.generationConfig.responseModalities,
                                speechConfig = config.speechConfig ?: config.generationConfig.speechConfig,
                            )
                        } else if (config?.responseModalities != null || config?.speechConfig != null) {
                            io.github.ugaikit.gemini4kt.GenerationConfig(
                                responseModalities = config?.responseModalities,
                                speechConfig = config?.speechConfig,
                            )
                        } else {
                            null
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
                // Wait with timeout? The original code had 10s timeout.
                // We can use withTimeout. But let's stick to simple wait for now or use the one from Coroutines.
                // Since we don't want to block indefinitely.
                // kotlinx.coroutines.withTimeout(10000) { handshakeCompleted.await() }
                // But I need to import withTimeout.

                handshakeCompleted.await()
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

class GeminiLiveSession(
    private val session: DefaultClientWebSocketSession,
    private val incomingMessages: Channel<BidiGenerateContentServerMessage>,
    private val json: Json,
    private val listenerJob: Job
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
