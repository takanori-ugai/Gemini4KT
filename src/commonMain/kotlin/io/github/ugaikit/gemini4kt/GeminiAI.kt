package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@OptIn(ExperimentalJsExport::class)
@JsExport
class GeminiAI(
    private val client: HttpClient? = null,
    private val apiKey: String? = null,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }

    private val httpClient = client ?: createHttpClient(json)

    private suspend fun getApiKey(): String =
        apiKey ?: io.github.ugaikit.gemini4kt
            .getApiKey()

    @JsName("createInteraction")
    suspend fun createInteraction(request: CreateInteractionRequest): Interaction {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.post("v1beta/interactions") {
                url {
                    parameters.append("key", apiKey)
                }
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody))
        }

        return response.body()
    }

    @JsName("getInteraction")
    suspend fun getInteraction(id: String): Interaction {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.get("v1beta/interactions/$id") {
                url {
                    parameters.append("key", apiKey)
                }
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody))
        }
        return response.body()
    }

    @JsName("deleteInteraction")
    suspend fun deleteInteraction(id: String) {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.delete("v1beta/interactions/$id") {
                url {
                    parameters.append("key", apiKey)
                }
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody))
        }
    }

    @JsName("cancelInteraction")
    suspend fun cancelInteraction(id: String): Interaction {
        val apiKey = getApiKey()
        val response: HttpResponse =
            httpClient.post("v1beta/interactions/$id/cancel") {
                url {
                    parameters.append("key", apiKey)
                }
            }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw GeminiException(parseError(errorBody))
        }
        return response.body()
    }

    private fun parseError(body: String): GeminiError =
        try {
            json.decodeFromString<GeminiErrorResponse>(body).error
        } catch (e: Exception) {
            GeminiError(INTERNAL_SERVER_ERROR, "Unknown error: $body", "UNKNOWN")
        }

    companion object {
        private const val INTERNAL_SERVER_ERROR = 500
    }
}
