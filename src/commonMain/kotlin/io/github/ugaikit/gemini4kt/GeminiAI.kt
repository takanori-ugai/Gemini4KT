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
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
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
