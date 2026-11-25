package io.github.ugaikit.gemini4kt.genie

import io.github.ugaikit.gemini4kt.buildUrl
import io.github.ugaikit.gemini4kt.live.closeLiveWebSocket
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal suspend fun HttpClient.openGenieWebSocket(
    id: String,
    credential: String,
    json: Json,
): GenieSession {
    val token = credential.takeIf { it.startsWith("auth_tokens/") }
    val url =
        buildUrl(
            "wss://generativelanguage.googleapis.com/v1beta",
            listOf("interactions", "live"),
            mapOf("auth_token" to token),
        )
    val client = config { install(WebSockets) }
    var connectingSocket: DefaultClientWebSocketSession? = null
    return try {
        val socket =
            client.webSocketSession(url) {
                if (token == null) header("x-goog-api-key", credential)
            }
        connectingSocket = socket
        val connect =
            buildJsonObject {
                put("event_type", "interaction.connect")
                put("id", id)
            }
        socket.send(Frame.Text(connect.toString()))
        val transport =
            GenieTransport(
                events =
                    socket.incoming.receiveAsFlow().mapNotNull { frame ->
                        when (frame) {
                            is Frame.Text -> frame.readText()
                            is Frame.Binary -> frame.readBytes().decodeToString()
                            else -> null
                        }
                    },
                send = { socket.send(Frame.Text(it)) },
                close = {
                    try {
                        socket.closeLiveWebSocket()
                    } finally {
                        client.close()
                    }
                },
            )
        GenieSession(id, transport, json)
    } catch (failure: Throwable) {
        connectingSocket?.cancel()
        // The handshake runs in the derived client's scope.
        client.coroutineContext.cancel()
        client.close()
        throw failure
    }
}
