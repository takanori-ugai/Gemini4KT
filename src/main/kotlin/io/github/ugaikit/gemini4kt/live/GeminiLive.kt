package io.github.ugaikit.gemini4kt.live

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.CompletionStage
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
    private val client = HttpClient.newHttpClient()
    private var webSocket: WebSocket? = null
    private val incomingMessages = Channel<BidiGenerateContentServerMessage>(Channel.UNLIMITED)
    private val scope = CoroutineScope(Dispatchers.IO)
    private var connectionJob: Job? = null

    // Base URL for WebSocket connection.
    // Note: The guide uses "ws" resource for WebSocket.
    // wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent
    private val wsUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"

    /**
     * Connects to the Live API and sends the initial setup message.
     */
    suspend fun connect(setup: BidiGenerateContentSetup? = null): GeminiLiveSession {
        val uri = URI("$wsUrl?key=$apiKey")

        val latch = CountDownLatch(1)

        // Better implementation of Listener
        val robustListener =
            object : WebSocket.Listener {
                val buffer = StringBuilder()

                override fun onOpen(webSocket: WebSocket) {
                    logger.info { "WebSocket connection opened" }
                    latch.countDown()
                    webSocket.request(1)
                }

                override fun onText(
                    webSocket: WebSocket,
                    data: CharSequence,
                    last: Boolean,
                ): CompletionStage<*> {
                    buffer.append(data)
                    if (last) {
                        val fullMessage = buffer.toString()
                        buffer.clear()
                        logger.debug { "Received full message: $fullMessage" }
                        try {
                            val message = json.decodeFromString<BidiGenerateContentServerMessage>(fullMessage)
                            scope.launch {
                                incomingMessages.send(message)
                            }
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to parse message" }
                        }
                    }
                    webSocket.request(1)
                    return java.util.concurrent.CompletableFuture
                        .completedFuture(null)
                }

                override fun onClose(
                    webSocket: WebSocket,
                    statusCode: Int,
                    reason: String,
                ): CompletionStage<*> {
                    logger.info { "WebSocket closed: $statusCode $reason" }
                    incomingMessages.close()
                    return super.onClose(webSocket, statusCode, reason)
                }

                override fun onError(
                    webSocket: WebSocket,
                    error: Throwable,
                ) {
                    logger.error(error) { "WebSocket error" }
                    incomingMessages.close(error)
                    super.onError(webSocket, error)
                }
            }

        webSocket =
            client
                .newWebSocketBuilder()
                .buildAsync(uri, robustListener)
                .join()

        // Wait for connection
        if (!latch.await(10, TimeUnit.SECONDS)) {
            // Since we don't have a GeminiError object here, we'll create a generic one or just throw the exception with a message.
            // The GeminiException constructor expects a GeminiError.
            // But checking GeminiException.kt might reveal other constructors or we should mock one.
            // Actually, let's look at GeminiException.kt.
            // Assuming I can't look at it right now, I'll pass a dummy error or just RuntimeException if GeminiException is strict.
            // But wait, the error said: Argument type mismatch: actual type is 'String', but 'GeminiError' was expected.
            // So GeminiException(String) constructor does not exist.

            throw RuntimeException("Timeout waiting for WebSocket connection")
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
        webSocket?.sendText(jsonMessage, true)

        return GeminiLiveSession(webSocket!!, incomingMessages, json)
    }
}

class GeminiLiveSession(
    private val webSocket: WebSocket,
    private val incomingMessages: Channel<BidiGenerateContentServerMessage>,
    private val json: Json,
) {
    /**
     * Sends a client content message.
     */
    fun sendClientContent(content: BidiGenerateContentClientContent) {
        val msg = BidiGenerateContentClientMessage(clientContent = content)
        send(msg)
    }

    /**
     * Sends a realtime input message.
     */
    fun sendRealtimeInput(input: BidiGenerateContentRealtimeInput) {
        val msg = BidiGenerateContentClientMessage(realtimeInput = input)
        send(msg)
    }

    /**
     * Sends a tool response message.
     */
    fun sendToolResponse(response: BidiGenerateContentToolResponse) {
        val msg = BidiGenerateContentClientMessage(toolResponse = response)
        send(msg)
    }

    private fun send(msg: BidiGenerateContentClientMessage) {
        val txt = json.encodeToString(msg)
        logger.debug { "Sending message: $txt" }
        webSocket.sendText(txt, true)
    }

    /**
     * Receives messages from the server.
     */
    fun receive(): Flow<BidiGenerateContentServerMessage> =
        flow {
            for (msg in incomingMessages) {
                emit(msg)
            }
        }

    /**
     * Closes the session.
     */
    fun close() {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Done")
    }
}
