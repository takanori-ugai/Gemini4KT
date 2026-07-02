package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.agent.Agent
import io.github.ugaikit.gemini4kt.agent.CreateAgentRequest
import io.github.ugaikit.gemini4kt.agent.ListAgentsResponse
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
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
            httpClient.post("$baseUrl/interactions") {
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
                .preparePost("$baseUrl/interactions?alt=sse") {
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
            httpClient.get("$baseUrl/interactions/$id") {
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
            httpClient.delete("$baseUrl/interactions/$id") {
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
            httpClient.post("$baseUrl/interactions/$id/cancel") {
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
            httpClient.get("$baseUrl/files/$envId") {
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
            httpClient.post("$baseUrl/agents") {
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
            httpClient.get("$baseUrl/agents/$id") {
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
            httpClient.delete("$baseUrl/agents/$id") {
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
            httpClient.get("$baseUrl/agents") {
                header("x-goog-api-key", apiKey)
                header("Api-Revision", API_REVISION)
                parameter("pageSize", pageSize)
                if (pageToken != null) {
                    parameter("pageToken", pageToken)
                }
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
            GeminiError(statusCode, "Unknown error: $body", "UNKNOWN")
        }

    fun close() {
        if (ownsHttpClient) {
            httpClient.close()
        }
    }
}
