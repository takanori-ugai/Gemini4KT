package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.agent.Agent
import io.github.ugaikit.gemini4kt.agent.CreateAgentRequest
import io.github.ugaikit.gemini4kt.agent.ListAgentsResponse
import io.github.ugaikit.gemini4kt.decisions.CreateDecisionRequest
import io.github.ugaikit.gemini4kt.decisions.DecisionResponse
import io.github.ugaikit.gemini4kt.genie.GenieSession
import io.github.ugaikit.gemini4kt.genie.GenieTransport
import io.github.ugaikit.gemini4kt.genie.GenieWebRtcPeer
import io.github.ugaikit.gemini4kt.genie.InteractionAuthToken
import io.github.ugaikit.gemini4kt.genie.openGenieWebSocket
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.webhooks.CreateWebhookRequest
import io.github.ugaikit.gemini4kt.webhooks.ListWebhooksResponse
import io.github.ugaikit.gemini4kt.webhooks.Webhook
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.js.JsName

class GeminiAI(
    private val client: HttpClient? = null,
    private val apiKey: String? = null,
) {
    init {
        require(apiKey == null || apiKey.isNotBlank()) { "apiKey must not be blank." }
    }

    companion object {
        private const val API_REVISION = "2026-05-20"
    }

    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            explicitNulls = false
        }

    private val httpClient = client ?: createHttpClient(json)
    private val ownsHttpClient = client == null

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"

    private suspend fun getApiKey(): String =
        requireNonBlankCredential(
            apiKey ?: io.github.ugaikit.gemini4kt
                .getApiKey(),
            "GeminiAI apiKey",
        )

    /**
     * Generates speech from text with the Gemini text-to-speech model.
     *
     * The generated audio is returned as base64 in the model output step's content data.
     * @param text Text to speak.
     * @param voice Prebuilt voice name, for example `Kore` or `Puck`.
     * @param style Optional description of the speaking style.
     * @param model TTS model to use.
     */
    @JsName("generateSpeech")
    suspend fun generateSpeech(
        text: String,
        voice: String = "Kore",
        style: String? = null,
        model: String = "gemini-3.8-flash-tts",
    ): Interaction {
        require(text.isNotBlank()) { "text must not be blank." }
        require(voice.isNotBlank()) { "voice must not be blank." }
        val apiKey = getApiKey()
        val requestBody =
            buildJsonObject {
                put("model", model)
                putJsonArray("input") {
                    add(
                        buildJsonObject {
                            put("type", "user_input")
                            putJsonArray("content") {
                                add(
                                    buildJsonObject {
                                        put("type", "text")
                                        put("text", text)
                                        if (style != null) {
                                            putJsonArray("annotations") {
                                                add(
                                                    buildJsonObject {
                                                        put("type", "speech_metadata")
                                                        put("style", style)
                                                    },
                                                )
                                            }
                                        }
                                    },
                                )
                            }
                        },
                    )
                }
                putJsonObject("response_format") { put("type", "audio") }
                putJsonObject("generation_config") {
                    putJsonArray("speech_config") {
                        add(buildJsonObject { put("voice", voice) })
                    }
                }
            }
        val response =
            httpClient.post(buildUrl(baseUrl, listOf("interactions"))) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }
        return response.body()
    }

    /** Lists prebuilt and saved voices, optionally applying catalog filters. */
    @JsName("listVoices")
    suspend fun listVoices(options: ListVoicesOptions = ListVoicesOptions()): ListVoicesResponse {
        val apiKey = getApiKey()
        val url =
            URLBuilder(baseUrl)
                .apply {
                    appendPathSegments("voices")

                    fun addValues(
                        name: String,
                        values: Array<String>?,
                    ) {
                        values?.forEach { parameters.append(name, it) }
                    }
                    addValues("language_code", options.languageCode)
                    addValues("region_code", options.regionCode)
                    addValues("accent", options.accent)
                    addValues("gender", options.gender)
                    addValues("pitch", options.pitch)
                    addValues("persona", options.persona)
                    addValues("context", options.contexts)
                    addValues("type", options.type)
                    options.search?.let { parameters.append("search", it) }
                    options.pageSize?.let { parameters.append("page_size", it.toString()) }
                    options.pageToken?.let { parameters.append("page_token", it) }
                }.buildString()
        val response = httpClient.get(url) { header("x-goog-api-key", apiKey) }
        if (!response.status.isSuccess()) {
            throw GeminiException(parseError(response.bodyAsText(), response.status.value))
        }
        return response.body()
    }

    /** Creates a persistent custom voice from a natural-language description. */
    @JsName("createDesignedVoice")
    suspend fun createDesignedVoice(
        description: String,
        displayName: String,
        model: String = "gemini-3.8-flash-tts",
        gender: String? = null,
        languageCode: String? = null,
    ): Voice {
        require(description.isNotBlank()) { "description must not be blank." }
        require(displayName.isNotBlank()) { "displayName must not be blank." }
        val voice =
            buildJsonObject {
                put("model", model)
                put("type", "prompted")
                put("display_name", displayName)
                gender?.let { put("gender", it) }
                languageCode?.let { put("language_code", it) }
                putJsonObject("prompted") { put("input", description) }
            }
        return createVoice(voice, store = true)
    }

    /**
     * Creates an independent stored voice by modifying a prebuilt or existing prompted voice.
     *
     * @param baseVoice Prebuilt voice name, catalog ID, or stored prompted `voice_...` ID.
     * @param instructions Natural-language description of the relative changes to make.
     * @param displayName Optional name for the new voice.
     * @param model Voice creation model; the resulting ID also supports Flash-Lite TTS synthesis.
     */
    @JsName("createRemixedVoice")
    suspend fun createRemixedVoice(
        baseVoice: String,
        instructions: String,
        displayName: String? = null,
        model: String = "gemini-3.8-flash-tts",
    ): Voice {
        require(baseVoice.isNotBlank()) { "baseVoice must not be blank." }
        require(instructions.isNotBlank()) { "instructions must not be blank." }
        require(displayName == null || displayName.isNotBlank()) { "displayName must not be blank." }
        require(model.isNotBlank()) { "model must not be blank." }
        val voice =
            buildJsonObject {
                put("model", model)
                put("type", "prompted")
                displayName?.let { put("display_name", it) }
                putJsonObject("prompted") {
                    put("base_voice", baseVoice)
                    put("input", instructions)
                }
            }
        return createVoice(voice, store = true)
    }

    /** Creates a replicated voice from base64 WAV source and consent recordings. */
    @JsName("createReplicatedVoice")
    suspend fun createReplicatedVoice(
        sourceAudioBase64: String,
        consentAudioBase64: String,
        displayName: String,
        model: String = "gemini-3.8-flash-tts",
        store: Boolean = true,
        mimeType: String = "audio/wav",
    ): Voice {
        require(sourceAudioBase64.isNotBlank()) { "sourceAudioBase64 must not be blank." }
        require(consentAudioBase64.isNotBlank()) { "consentAudioBase64 must not be blank." }
        require(displayName.isNotBlank()) { "displayName must not be blank." }
        val voice =
            buildJsonObject {
                put("model", model)
                put("type", "replicated")
                put("display_name", displayName)
                putJsonObject("replicated") {
                    putJsonObject("source_audio") {
                        put("mime_type", mimeType)
                        put("data", sourceAudioBase64)
                    }
                    putJsonObject("consent_audio") {
                        put("mime_type", mimeType)
                        put("data", consentAudioBase64)
                    }
                }
            }
        return createVoice(voice, store)
    }

    private suspend fun createVoice(
        voice: JsonElement,
        store: Boolean,
    ): Voice {
        val apiKey = getApiKey()
        val body =
            buildJsonObject {
                put("store", store)
                put("voice", voice)
            }
        val response =
            httpClient.post(buildUrl(baseUrl, listOf("voices"))) {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        if (!response.status.isSuccess()) {
            throw GeminiException(parseError(response.bodyAsText(), response.status.value))
        }
        return response.body()
    }

    /** Gets a saved voice, including its preview audio when available. */
    @JsName("getVoice")
    suspend fun getVoice(id: String): Voice {
        val response =
            httpClient.get(buildUrl(baseUrl, listOf("voices") + normalizeResourcePathSegments(id, "voices"))) {
                header("x-goog-api-key", getApiKey())
            }
        if (!response.status.isSuccess()) {
            throw GeminiException(parseError(response.bodyAsText(), response.status.value))
        }
        return response.body()
    }

    /** Deletes a saved custom voice. */
    @JsName("deleteVoice")
    suspend fun deleteVoice(id: String) {
        val response =
            httpClient.delete(buildUrl(baseUrl, listOf("voices") + normalizeResourcePathSegments(id, "voices"))) {
                header("x-goog-api-key", getApiKey())
            }
        if (!response.status.isSuccess()) {
            throw GeminiException(parseError(response.bodyAsText(), response.status.value))
        }
    }

    /** Evaluates typed binary, categorical, and rubric questions without generating text tokens. */
    @JsName("createDecision")
    suspend fun createDecision(request: CreateDecisionRequest): DecisionResponse {
        val response =
            httpClient.post(buildUrl(baseUrl, listOf("decisions"))) {
                header("x-goog-api-key", getApiKey())
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(CreateDecisionRequest.serializer(), request))
            }
        if (!response.status.isSuccess()) {
            throw GeminiException(parseError(response.bodyAsText(), response.status.value))
        }
        return json.decodeFromString(DecisionResponse.serializer(), response.bodyAsText())
    }

    /** Create an idle Genie 4 world. Send its initial prompt over the connected transport. */
    @JsName("createGenieInteraction")
    suspend fun createGenieInteraction(): Interaction =
        postGenieRequest(
            buildUrl(baseUrl, listOf("interactions")),
            getApiKey(),
            buildJsonObject { put("model", "genie-4") },
        )

    /** Mint a credential scoped to one interaction for a browser or other delegated client. */
    @JsName("createInteractionAuthToken")
    suspend fun createInteractionAuthToken(id: String): InteractionAuthToken =
        postGenieRequest(
            buildUrl(baseUrl, listOf("interactions", genieInteractionId(id), "auth_tokens")),
            getApiKey(),
            buildJsonObject {},
        )

    /** Connect to Genie 4 and receive media as base64 video and audio deltas. */
    @JsName("connectGenieWebSocket")
    suspend fun connectGenieWebSocket(
        id: String,
        authToken: InteractionAuthToken? = null,
        timeoutMs: Long = 30_000,
    ): GenieSession =
        withTimeout(timeoutMs) {
            httpClient.openGenieWebSocket(genieInteractionId(id), authToken?.authToken ?: getApiKey(), json)
        }

    /** Exchange SDP and connect a platform WebRTC peer. Media plays through its native tracks. */
    @JsName("connectGenieWebRtc")
    suspend fun connectGenieWebRtc(
        id: String,
        peer: GenieWebRtcPeer,
        authToken: InteractionAuthToken? = null,
        timeoutMs: Long = 30_000,
    ): GenieSession {
        val interactionId = genieInteractionId(id)
        var connected = false
        try {
            withTimeout(timeoutMs) {
                val offer = peer.createOffer()
                val answer =
                    postGenieRequest<GenieSdpAnswer>(
                        buildUrl(baseUrl, listOf("interactions", interactionId)) + ":webrtc",
                        authToken?.authToken ?: getApiKey(),
                        buildJsonObject { put("offer_sdp", offer) },
                        sendRevision = false,
                    )
                peer.acceptAnswer(answer.sdp)
            }
            val session = GenieSession(interactionId, GenieTransport(peer.events, peer::send, peer::close), json)
            connected = true
            return session
        } finally {
            if (!connected) withContext(NonCancellable) { runCatching { peer.close() } }
        }
    }

    private fun genieInteractionId(id: String): String =
        requireNotNull(normalizeResourcePathSegments(id, "interactions").singleOrNull()) {
            "Expected an interaction ID or interactions/{id} resource name."
        }

    private suspend inline fun <reified T> postGenieRequest(
        url: String,
        credential: String,
        payload: JsonElement,
        sendRevision: Boolean = true,
    ): T {
        val response =
            httpClient.post(url) {
                header("x-goog-api-key", credential)
                if (sendRevision) header("Api-Revision", API_REVISION)
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) throw GeminiException(parseError(body, response.status.value))
        return json.decodeFromString<T>(body)
    }

    @JsName("createInteraction")
    suspend fun createInteraction(request: CreateInteractionRequest): Interaction {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.post(buildUrl(baseUrl, listOf("interactions"))) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }

        return response.body()
    }

    @JsName("streamInteraction")
    fun streamInteraction(request: CreateInteractionRequest): Flow<JsonElement> =
        channelFlow {
            val apiKey = getApiKey()
            httpClient
                .preparePost(buildUrl(baseUrl, listOf("interactions"), mapOf("alt" to "sse"))) {
                    header("x-goog-api-key", apiKey)
                    header("Api-Revision", API_REVISION)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.execute { response ->
                    response.consumeInteractionEvents { send(it) }
                }
        }

    /**
     * Reads a stored interaction's event stream, optionally resuming after the given event ID.
     */
    @JsName("resumeInteractionStream")
    fun resumeInteractionStream(
        id: String,
        lastEventId: String? = null,
    ): Flow<JsonElement> =
        channelFlow {
            val query = mutableMapOf("stream" to "true")
            lastEventId?.let { query["last_event_id"] = it }
            val path = listOf("interactions") + normalizeResourcePathSegments(id, "interactions")
            val apiKey = getApiKey()
            httpClient
                .prepareGet(buildUrl(baseUrl, path, query)) {
                    header("x-goog-api-key", apiKey)
                    header("Api-Revision", API_REVISION)
                }.execute { response ->
                    response.consumeInteractionEvents { send(it) }
                }
        }

    private suspend fun HttpResponse.consumeInteractionEvents(emit: suspend (JsonElement) -> Unit) {
        if (!status.isSuccess()) {
            throw GeminiException(parseError(bodyAsText(), status.value))
        }

        bodyAsChannel().consumeServerSentEvents { payload ->
            if (payload.isBlank() || payload == "[DONE]") return@consumeServerSentEvents
            emit(json.decodeFromString<JsonElement>(payload))
        }
    }

    @JsName("getInteraction")
    suspend fun getInteraction(id: String): Interaction {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.get(buildUrl(baseUrl, listOf("interactions") + normalizeResourcePathSegments(id, "interactions"))) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }
        return response.body()
    }

    @JsName("deleteInteraction")
    suspend fun deleteInteraction(id: String) {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.delete(buildUrl(baseUrl, listOf("interactions") + normalizeResourcePathSegments(id, "interactions"))) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }
    }

    @JsName("cancelInteraction")
    suspend fun cancelInteraction(id: String): Interaction {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.post(
                buildUrl(
                    baseUrl,
                    listOf("interactions") + normalizeResourcePathSegments(id, "interactions") + listOf("cancel"),
                ),
            ) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }
        return response.body()
    }

    @JsName("downloadEnvironmentFiles")
    suspend fun downloadEnvironmentFiles(envId: String): ByteArray {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.get(buildUrl(baseUrl, listOf("files") + normalizeResourcePathSegments(envId, "files"))) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }

        return response.body()
    }

    @JsName("createAgent")
    suspend fun createAgent(request: CreateAgentRequest): Agent {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.post(buildUrl(baseUrl, listOf("agents"))) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }

        return response.body()
    }

    @JsName("getAgent")
    suspend fun getAgent(id: String): Agent {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.get(buildUrl(baseUrl, listOf("agents") + normalizeResourcePathSegments(id, "agents"))) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }

        return response.body()
    }

    @JsName("deleteAgent")
    suspend fun deleteAgent(id: String) {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.delete(buildUrl(baseUrl, listOf("agents") + normalizeResourcePathSegments(id, "agents"))) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }
    }

    @JsName("listAgents")
    suspend fun listAgents(
        pageSize: Int = 10,
        pageToken: String? = null,
    ): ListAgentsResponse {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.get(
                buildUrl(
                    baseUrl,
                    listOf("agents"),
                    mapOf(
                        "pageSize" to pageSize.toString(),
                        "pageToken" to pageToken,
                    ),
                ),
            ) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }

        return response.body()
    }

    @JsName("createWebhook")
    suspend fun createWebhook(
        request: CreateWebhookRequest,
        webhookId: String? = null,
    ): Webhook {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.post(
                buildUrl(
                    baseUrl,
                    listOf("webhooks"),
                    mapOf("webhook_id" to webhookId),
                ),
            ) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }

        return response.body()
    }

    @JsName("getWebhook")
    suspend fun getWebhook(id: String): Webhook {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.get(buildUrl(baseUrl, listOf("webhooks") + normalizeResourcePathSegments(id, "webhooks"))) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }

        return response.body()
    }

    @JsName("deleteWebhook")
    suspend fun deleteWebhook(id: String) {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.delete(buildUrl(baseUrl, listOf("webhooks") + normalizeResourcePathSegments(id, "webhooks"))) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }
    }

    @JsName("listWebhooks")
    suspend fun listWebhooks(
        pageSize: Int = 10,
        pageToken: String? = null,
    ): ListWebhooksResponse {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.get(
                buildUrl(
                    baseUrl,
                    listOf("webhooks"),
                    mapOf(
                        "page_size" to pageSize.toString(),
                        "page_token" to pageToken,
                    ),
                ),
            ) {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody, response.status.value))
        }

        return response.body()
    }

    private fun parseError(
        body: String,
        statusCode: Int,
    ): GeminiError {
        val fallbackStatus = httpStatusErrorCode(statusCode)
        return try {
            val errorObject = json.parseToJsonElement(body).jsonObject["error"]?.jsonObject
                ?: return fallbackError(body, statusCode, fallbackStatus)
            val rawCode = errorObject["code"]?.jsonPrimitive
            val apiCode = rawCode?.takeIf { it.isString }?.contentOrNull
            val httpCode = rawCode?.intOrNull ?: statusCode
            val message = errorObject["message"]?.jsonPrimitive?.contentOrNull
                ?: "Unknown error: ${summarizeErrorBody(body, fallbackStatus)}"
            val status = errorObject["status"]?.jsonPrimitive?.contentOrNull
                ?: apiCode?.uppercase()
                ?: fallbackStatus.uppercase()
            val details = errorObject["details"]?.let { detailsElement ->
                runCatching { json.decodeFromJsonElement<List<GeminiErrorDetail>>(detailsElement) }.getOrNull()
            }
            GeminiError(
                code = httpCode,
                message = message,
                status = status,
                details = details,
                apiCode = apiCode,
            )
        } catch (_: Exception) {
            fallbackError(body, statusCode, fallbackStatus)
        }
    }

    private fun fallbackError(
        body: String,
        statusCode: Int,
        fallbackStatus: String,
    ): GeminiError =
        GeminiError(
            code = statusCode,
            message = "Unknown error: ${summarizeErrorBody(body, fallbackStatus)}",
            status = fallbackStatus.uppercase(),
        )

    private fun httpStatusErrorCode(statusCode: Int): String =
        when (statusCode) {
            400 -> "bad_request"
            401 -> "unauthorized"
            402 -> "payment_required"
            403 -> "forbidden"
            404 -> "not_found"
            409 -> "conflict"
            416 -> "range_not_satisfiable"
            429 -> "too_many_requests"
            499 -> "client_closed_request"
            500 -> "internal_server_error"
            501 -> "not_implemented"
            503 -> "service_unavailable"
            504 -> "gateway_timeout"
            else -> "http_$statusCode"
        }

    fun close() {
        if (ownsHttpClient) {
            httpClient.close()
        }
    }
}

@Serializable
private data class GenieSdpAnswer(
    @SerialName("answer_sdp") val sdp: String,
)
