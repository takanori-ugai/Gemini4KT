package io.github.ugaikit.gemini4kt.genie

import io.github.ugaikit.gemini4kt.interaction.InteractionUsage
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** A single-interaction credential. Its string representation omits the credential. */
@Serializable
class InteractionAuthToken(
    @SerialName("auth_token") val authToken: String,
    @SerialName("expires_at") val expiresAt: String? = null,
)

/** A base64-encoded 16:9 JPEG or PNG used to seed a world. */
@Serializable
data class GenieReferenceImage(
    @SerialName("mime_type") val mimeType: String,
    val data: String,
)

/** Movement and camera controls. */
@Serializable
enum class GenieKey {
    @SerialName("key_w")
    W,

    @SerialName("key_a")
    A,

    @SerialName("key_s")
    S,

    @SerialName("key_d")
    D,

    @SerialName("arrow_up")
    ARROW_UP,

    @SerialName("arrow_down")
    ARROW_DOWN,

    @SerialName("arrow_left")
    ARROW_LEFT,

    @SerialName("arrow_right")
    ARROW_RIGHT,
}

/** How a native control key changes state. */
@Serializable
enum class GenieKeyAction {
    @SerialName("key_down")
    DOWN,

    @SerialName("key_up")
    UP,

    @SerialName("key_click")
    CLICK,
}

/** A realtime event. WebSocket media arrives in [delta] as base64 JPEG or stereo PCM data. */
@Serializable
data class GenieEvent(
    @SerialName("event_type") val eventType: String,
    @SerialName("event_id") val eventId: String? = null,
    val index: Int? = null,
    val delta: GenieMediaDelta? = null,
    val interaction: JsonObject? = null,
    val error: JsonObject? = null,
    val metadata: GenieEventMetadata? = null,
)

@Serializable
data class GenieMediaDelta(
    val type: String,
    val data: String? = null,
    val text: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    @SerialName("sample_rate") val sampleRate: Int? = null,
    val rate: Int? = null,
    val channels: Int? = null,
)

@Serializable
data class GenieEventMetadata(
    @SerialName("total_usage") val totalUsage: InteractionUsage? = null,
)

/**
 * A platform WebRTC peer. Create the `events` data channel and receive-only audio and video
 * transceivers before producing the offer. [acceptAnswer] must return after the data channel opens.
 */
interface GenieWebRtcPeer {
    val events: Flow<String>

    suspend fun createOffer(): String

    suspend fun acceptAnswer(sdp: String)

    suspend fun send(message: String)

    suspend fun close()
}

internal class GenieTransport(
    val events: Flow<String>,
    val send: suspend (String) -> Unit,
    val close: suspend () -> Unit,
)

/** Control a connected world and consume its server events with one collector. */
class GenieSession internal constructor(
    val id: String,
    private val transport: GenieTransport,
    private val json: Json,
) {
    /** Seed a newly connected world once, before sending movement or text events. */
    suspend fun setupWorld(
        text: String? = null,
        referenceImage: GenieReferenceImage? = null,
    ) {
        require(!text.isNullOrBlank() || referenceImage != null) { "A world prompt or reference image is required." }
        sendEvent("world_setup") {
            if (!text.isNullOrBlank()) put("world_prompt", buildJsonObject { put("text", text) })
            referenceImage?.let {
                put("reference_image", json.encodeToJsonElement(GenieReferenceImage.serializer(), it))
            }
        }
    }

    /** Move or look around. Pair key-down events with key-up events. */
    suspend fun sendKey(
        key: GenieKey,
        action: GenieKeyAction,
    ) = sendEvent("user_action") {
        put("action", json.encodeToJsonElement(GenieKeyAction.serializer(), action))
        put("input", json.encodeToJsonElement(GenieKey.serializer(), key))
    }

    /** Apply an in-world text instruction. */
    suspend fun sendText(text: String) =
        sendEvent("input") {
            put(
                "content",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("type", "text")
                            put("text", text)
                        },
                    )
                },
            )
        }

    fun receive(): Flow<GenieEvent> =
        transport.events.transformWhile { message ->
            val event = json.decodeFromString<GenieEvent>(message)
            emit(event)
            event.eventType !in terminalEvents
        }

    /** Request the end of generation and billing. Read the terminal server event before closing. */
    suspend fun complete() = sendEvent("interaction.complete")

    /** Abort generation. Read the terminal server event before closing. */
    suspend fun cancel() = sendEvent("interaction.cancel")

    /** Close the local transport. Call [complete] or [cancel] to stop generation on the server. */
    suspend fun close() = withContext(NonCancellable) { transport.close() }

    private suspend fun sendEvent(
        type: String,
        payload: JsonObjectBuilder.() -> Unit = {},
    ) = transport.send(
        buildJsonObject {
            put("event_type", type)
            payload()
        }.toString(),
    )

    private companion object {
        val terminalEvents =
            setOf("interaction.completed", "interaction.cancelled", "interaction.canceled", "reconnection_required")
    }
}
