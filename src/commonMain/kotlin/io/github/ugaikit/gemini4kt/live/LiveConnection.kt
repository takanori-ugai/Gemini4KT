package io.github.ugaikit.gemini4kt.live

import io.github.oshai.kotlinlogging.KLogger
import io.github.ugaikit.gemini4kt.X_GOOG_API_CLIENT
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json

/**
 * Shared live WebSocket connection state.
 */
internal data class LiveConnection<T>(
    val httpClient: HttpClient,
    val session: DefaultClientWebSocketSession,
    val incomingMessages: Channel<T>,
    val listenerJob: Job,
    val handshakeCompleted: CompletableDeferred<Unit>,
)

/**
 * Opens a Gemini live WebSocket, starts the frame listener, and returns the shared connection
 * state used by the live clients.
 */
internal suspend inline fun <reified T> openLiveConnection(
    url: String,
    apiKey: String,
    client: HttpClient? = null,
    json: Json,
    logger: KLogger,
    connectTimeoutMs: Long? = null,
    crossinline isHandshakeComplete: (T) -> Boolean,
): LiveConnection<T> {
    val httpClient =
        client?.config {
            install(WebSockets)
        } ?: HttpClient {
            install(WebSockets)
        }

    var session: DefaultClientWebSocketSession? = null
    try {
        session =
            if (connectTimeoutMs == null) {
                httpClient.webSocketSession(url) {
                    header("x-goog-api-key", apiKey)
                    header("x-goog-api-client", X_GOOG_API_CLIENT)
                }
            } else {
                withTimeout(connectTimeoutMs) {
                    httpClient.webSocketSession(url) {
                        header("x-goog-api-key", apiKey)
                        header("x-goog-api-client", X_GOOG_API_CLIENT)
                    }
                }
            }

        val activeSession = requireNotNull(session) { "WebSocket session was not initialized." }
        val incomingMessages = Channel<T>(Channel.BUFFERED)
        val handshakeCompleted = CompletableDeferred<Unit>()

        val listenerJob =
            activeSession.launch {
                try {
                    for (frame in activeSession.incoming) {
                        val text =
                            when (frame) {
                                is Frame.Text -> frame.readText()
                                is Frame.Binary -> decodeBinaryFrame(frame, logger) ?: continue
                                else -> continue
                            }

                        logger.debug { "Received live message (${text.length} chars)" }
                        processHandshakeMessage(
                            text,
                            handshakeCompleted,
                            incomingMessages,
                            json,
                            logger,
                            isHandshakeComplete,
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.error(e) { "WebSocket error" }
                    incomingMessages.close(e)
                    if (!handshakeCompleted.isCompleted) {
                        handshakeCompleted.completeExceptionally(e)
                    } else {
                        try {
                            activeSession.close()
                        } finally {
                            httpClient.close()
                        }
                    }
                } finally {
                    if (!handshakeCompleted.isCompleted) {
                        handshakeCompleted.completeExceptionally(
                            IllegalStateException("WebSocket closed before setupComplete was received."),
                        )
                    }
                    incomingMessages.close()
                }
            }

        return LiveConnection(httpClient, activeSession, incomingMessages, listenerJob, handshakeCompleted)
    } catch (e: Exception) {
        try {
            session?.close()
        } finally {
            httpClient.close()
        }
        throw e
    }
}
