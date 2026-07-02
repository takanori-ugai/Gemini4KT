package io.github.ugaikit.gemini4kt.live.music

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.ugaikit.gemini4kt.live.openLiveConnection
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

private val logger = KotlinLogging.logger {}

/**
 * Options for configuring the LiveMusic WebSocket connection.
 *
 * @property apiVersion API version used in the WebSocket endpoint path.
 * @property baseUrl Base URL used to construct the Live Music endpoint.
 */
data class LiveMusicOptions(
    val apiVersion: String = "v1alpha",
    val baseUrl: String = "https://generativelanguage.googleapis.com/",
    val allowCustomEndpoint: Boolean = false,
)

/**
 * Builds the WebSocket endpoint URL for the Live Music generative service.
 *
 * @param options Configuration containing the base URL and API version.
 * @return The full WebSocket URL for the BidiGenerateMusic endpoint, including protocol and apiVersion.
 */
private fun buildWebSocketUrl(options: LiveMusicOptions): String {
    val baseWithoutSlash = if (options.baseUrl.endsWith("/")) options.baseUrl.dropLast(1) else options.baseUrl
    val trustedHost = "generativelanguage.googleapis.com"
    val normalizedHost =
        baseWithoutSlash
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("wss://")
            .removePrefix("ws://")
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore("#")
            .substringBefore("@")
            .substringBefore(":")
    // Intentionally pin supported versions to known Live Music preview endpoints.
    // Revisit this list when the API adds/stabilizes new versions (for example, a GA v1).
    val trustedApiVersions = setOf("v1alpha", "v1beta")
    require(options.apiVersion in trustedApiVersions) {
        "Unsupported Live Music apiVersion '${options.apiVersion}'."
    }
    require(options.allowCustomEndpoint || normalizedHost == trustedHost) {
        "Custom Live Music endpoints require allowCustomEndpoint = true."
    }
    val wsBase =
        when {
            baseWithoutSlash.startsWith("http://") -> baseWithoutSlash.replaceFirst("http://", "ws://")
            baseWithoutSlash.startsWith("https://") -> baseWithoutSlash.replaceFirst("https://", "wss://")
            baseWithoutSlash.startsWith("ws://") || baseWithoutSlash.startsWith("wss://") -> baseWithoutSlash
            else -> "wss://$baseWithoutSlash"
        }
    return "$wsBase/ws/google.ai.generativelanguage.${options.apiVersion}.GenerativeService.BidiGenerateMusic"
}

/**
 * Client for interacting with the Gemini Live Music API over WebSockets.
 *
 * @param apiKey API key used for authentication.
 * @param model Model name or resource name used for live music generation.
 * @param options Connection options used to build the endpoint URL.
 * @param json JSON serializer used for request and response payloads.
 * @param client Optional externally managed HTTP client. When omitted, a client is created and
 * owned by each session.
 */
class LiveMusic(
    private val apiKey: String,
    private val model: String,
    private val options: LiveMusicOptions = LiveMusicOptions(),
    private val json: Json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        },
    private val client: HttpClient? = null,
) {
    private val wsUrl = buildWebSocketUrl(options)

    /**
     * Establishes a WebSocket connection to the Live Music API, sends the initial setup message,
     * awaits handshake completion, and returns an active session.
     *
     * The method configures or creates an HttpClient with WebSockets, opens a WebSocket session
     * with authentication headers, launches a listener to process incoming messages, sends the
     * model setup payload, and waits for the server's setup-complete signal before returning.
     *
     * @param handshakeTimeoutMs Timeout in milliseconds for handshake completion.
     * @param connectTimeoutMs Timeout in milliseconds for establishing the WebSocket connection.
     * @return Active [LiveMusicSession] for controlling playback and receiving server events.
     */
    suspend fun connect(
        handshakeTimeoutMs: Long = 10_000,
        connectTimeoutMs: Long = 10_000,
    ): LiveMusicSession {
        logger.info { "Connecting to WebSocket at $wsUrl" }

        val connection =
            openLiveConnection<LiveMusicServerMessage>(
                url = wsUrl,
                apiKey = apiKey,
                client = client,
                json = json,
                logger = logger,
                connectTimeoutMs = connectTimeoutMs,
            ) { message: LiveMusicServerMessage ->
                message.setupComplete != null
            }

        try {
            logger.info { "WebSocket session established." }

            // Send Setup Message
            // Ensure model has "models/" prefix if not present
            val modelName = if (model.startsWith("models/")) model else "models/$model"
            val setup = LiveMusicClientSetup(model = modelName)

            val clientMessage = LiveMusicClientMessage(setup = setup)
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

            return LiveMusicSession(
                connection.session,
                connection.incomingMessages,
                json,
                connection.listenerJob,
                connection.httpClient,
                true,
            )
        } catch (e: Exception) {
            logger.error(e) { "Error in connect method" }
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
 * Active Live Music session returned by [LiveMusic.connect].
 *
 * @param session Underlying WebSocket session.
 * @param incomingMessages Channel of decoded server messages.
 * @param json JSON serializer used for outbound payloads.
 * @param listenerJob Background job that reads and decodes incoming frames.
 * @param httpClient HTTP client associated with the session.
 * @param ownsClient Whether this session is responsible for closing [httpClient].
 */
class LiveMusicSession(
    private val session: WebSocketSession,
    private val incomingMessages: Channel<LiveMusicServerMessage>,
    private val json: Json,
    private val listenerJob: Job,
    private val httpClient: HttpClient,
    private val ownsClient: Boolean,
) {
    /**
     * Sets inputs to steer music generation. Updates the session's current weighted prompts.
     *
     * @param weightedPrompts Prompt list with relative weights.
     */
    suspend fun setWeightedPrompts(weightedPrompts: List<WeightedPrompt>) {
        require(weightedPrompts.isNotEmpty()) {
            "Weighted prompts must contain at least one entry."
        }
        val clientContent = LiveMusicClientContent(weightedPrompts = weightedPrompts)
        val msg = LiveMusicClientMessage(clientContent = clientContent)
        send(msg)
    }

    /**
     * Sets a configuration to the model. Updates the session's current music generation config.
     *
     * @param config Generation configuration values to apply.
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
        logger.debug { "Sending live music message (${txt.length} chars)" }
        session.send(Frame.Text(txt))
    }

    /**
     * Returns a cold [Flow] view of decoded server messages for this session.
     */
    fun receive(): Flow<LiveMusicServerMessage> = incomingMessages.receiveAsFlow()

    /**
     * Closes the WebSocket session and cleans up session resources.
     *
     * Cancels the listener coroutine, closes the incoming message channel, and closes the associated
     * HttpClient if this session owns it.
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
