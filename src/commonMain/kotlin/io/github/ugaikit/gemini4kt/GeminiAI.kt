package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.agent.Agent
import io.github.ugaikit.gemini4kt.agent.CreateAgentRequest
import io.github.ugaikit.gemini4kt.agent.ListAgentsResponse
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
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
                    if (!response.status.isSuccess()) {
                        val errorBody = response.bodyAsText()
                        throw GeminiException(parseError(errorBody, response.status.value))
                    }

                    response.bodyAsChannel().consumeServerSentEvents { payload ->
                        if (payload.isBlank() || payload == "[DONE]") return@consumeServerSentEvents
                        send(json.decodeFromString<JsonElement>(payload))
                    }
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
    ): GeminiError =
        try {
            json.decodeFromString<GeminiErrorResponse>(body).error
        } catch (e: Exception) {
            GeminiError(statusCode, "Unknown error: ${summarizeErrorBody(body, "unknown")}", "UNKNOWN")
        }

    fun close() {
        if (ownsHttpClient) {
            httpClient.close()
        }
    }
}
