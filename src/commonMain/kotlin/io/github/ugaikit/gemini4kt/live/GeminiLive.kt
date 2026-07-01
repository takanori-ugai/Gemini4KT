package io.github.ugaikit.gemini4kt.live

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Holds the logger.
 */
private val logger = KotlinLogging.logger {}

/**
 * Client for interacting with the Gemini Live API over WebSockets.
 *
 * @param apiKey API key used for authentication.
 * @param model Model name or resource name to use for the session.
 * @param config Optional default setup configuration applied when `connect` is called without an
 * explicit setup object.
 * @param json JSON serializer used for request and response payloads.
 * @param client Optional externally managed HTTP client. When omitted, a client is created and
 * owned by each session.
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
        "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta" +
            ".GenerativeService.BidiGenerateContent"

    /**
     * Opens a WebSocket connection to the Gemini Live API, sends the initial setup message, and
     * establishes a session for ongoing bidirectional communication.
     *
     * If `setup` is null, a setup message is constructed from the instance `model` and `config`
     * (including generationConfig, systemInstruction, and tools) and sent instead.
     *
     * @param setup Optional explicit setup message to send as the initial client payload; when
     * omitted, a setup is derived from the instance configuration.
     * @param handshakeTimeoutMs Timeout in milliseconds to wait for the `setupComplete` message
     * before treating the connection as failed.
     * @return Active [GeminiLiveSession] for sending client messages and receiving server events.
     */
    suspend fun connect(
        setup: BidiGenerateContentSetup? = null,
        handshakeTimeoutMs: Long = 10_000,
    ): GeminiLiveSession {
        logger.info { "Connecting to WebSocket at $wsUrl" }

        val connection =
            openLiveConnection<BidiGenerateContentServerMessage>(
                url = wsUrl,
                apiKey = apiKey,
                client = client,
                json = json,
                logger = logger,
            ) { message -> message.setupComplete != null }

        try {
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
            logger.debug { "Sending setup message (${jsonMessage.length} chars)" }
            connection.session.send(Frame.Text(jsonMessage))

            // Wait for setup complete message
            try {
                withTimeout(handshakeTimeoutMs) {
                    connection.handshakeCompleted.await()
                }
            } catch (e: Exception) {
                logger.error(e) { "Error waiting for SetupComplete" }
                // Close resources
                connection.listenerJob.cancelAndJoin()
                connection.session.close()
                throw e
            }

            return GeminiLiveSession(
                connection.session,
                connection.incomingMessages,
                json,
                connection.listenerJob,
                connection.httpClient,
                true,
            )
        } catch (e: Exception) {
            try {
                connection.session.close()
            } finally {
                connection.httpClient.close()
            }
            throw e
        }
    }
}

/**
 * Active Gemini Live session returned by [GeminiLive.connect].
 *
 * @param session Underlying WebSocket session.
 * @param incomingMessages Channel of decoded server messages.
 * @param json JSON serializer used for outbound payloads.
 * @param listenerJob Background job that reads and decodes incoming frames.
 * @param httpClient HTTP client associated with the session.
 * @param ownsClient Whether this session is responsible for closing [httpClient].
 */
class GeminiLiveSession(
    private val session: WebSocketSession,
    private val incomingMessages: Channel<BidiGenerateContentServerMessage>,
    private val json: Json,
    private val listenerJob: Job,
    private val httpClient: HttpClient,
    private val ownsClient: Boolean,
) {
    /**
     * Sends a client content update message.
     *
     * @param content Client content payload to send.
     */
    suspend fun sendClientContent(content: BidiGenerateContentClientContent) {
        val msg = BidiGenerateContentClientMessage(clientContent = content)
        send(msg)
    }

    /**
     * Sends a realtime input message.
     *
     * @param input Realtime input payload.
     */
    suspend fun sendRealtimeInput(input: BidiGenerateContentRealtimeInput) {
        val msg = BidiGenerateContentClientMessage(realtimeInput = input)
        send(msg)
    }

    /**
     * Sends a tool response message.
     *
     * @param response Tool response payload.
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
        logger.debug { "Sending live message (${txt.length} chars)" }
        session.send(Frame.Text(txt))
    }

    /**
     * Returns a cold [Flow] view of decoded server messages for this session.
     */
    fun receive(): Flow<BidiGenerateContentServerMessage> = incomingMessages.receiveAsFlow()

    /**
     * Closes the WebSocket session and releases associated resources.
     */
    suspend fun close() {
        try {
            session.close()
        } finally {
            listenerJob.cancelAndJoin()
            incomingMessages.close()
            if (ownsClient) {
                httpClient.close()
            }
        }
    }
}
