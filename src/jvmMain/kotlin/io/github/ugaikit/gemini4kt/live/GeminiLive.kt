package io.github.ugaikit.gemini4kt.live

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
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
) {
    private val client =
        HttpClient {
            install(WebSockets)
        }

    // Base URL for WebSocket connection.
    private val wsUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"

    /**
     * Connects to the Live API and sends the initial setup message.
     */
    suspend fun connect(setup: BidiGenerateContentSetup? = null): GeminiLiveSession {
        val session = client.webSocketSession(urlString = "$wsUrl?key=$apiKey")

        logger.info { "WebSocket connection opened" }

        val incomingMessages = Channel<BidiGenerateContentServerMessage>(Channel.UNLIMITED)
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                for (frame in session.incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        logger.debug { "Received message: $text" }
                        try {
                            val message = json.decodeFromString<BidiGenerateContentServerMessage>(text)
                            incomingMessages.send(message)
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to parse message" }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "WebSocket error" }
                incomingMessages.close(e)
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

                BidiGenerateContentSetup(
                    model = "models/$model", // Ensure model format
                    generationConfig = generationConfig,
                    systemInstruction = config?.systemInstruction,
                    tools = config?.tools,
                )
            }

        val clientMessage = BidiGenerateContentClientMessage(setup = setupMessage)
        val jsonMessage = json.encodeToString(clientMessage)
        logger.debug { "Sending setup message: $jsonMessage" }
        session.send(Frame.Text(jsonMessage))

        return GeminiLiveSession(session, incomingMessages, json, scope)
    }
}

class GeminiLiveSession(
    private val session: DefaultClientWebSocketSession,
    private val incomingMessages: Channel<BidiGenerateContentServerMessage>,
    private val json: Json,
    private val scope: CoroutineScope,
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
        session.close(CloseReason(CloseReason.Codes.NORMAL, "Done"))
        scope.cancel()
    }
}
