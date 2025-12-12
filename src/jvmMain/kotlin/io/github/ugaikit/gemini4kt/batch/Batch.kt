package io.github.ugaikit.gemini4kt.batch

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.ugaikit.gemini4kt.GeminiErrorResponse
import io.github.ugaikit.gemini4kt.GeminiException
import io.github.ugaikit.gemini4kt.createHttpClient
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val logger = KotlinLogging.logger {}

/**
 * Client for interacting with the Gemini Batch API.
 *
 * @property apiKey The API key used for authenticating requests.
 */
class Batch(
    private val apiKey: String,
    private val client: HttpClient = createHttpClient(Json { ignoreUnknownKeys = true }),
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val bUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val baseUrl = "$bUrl/models"

    companion object {
        private const val PREVIEW_LENGTH = 100
    }

    /**
     * Creates a batch job for content generation.
     *
     * @param model The model to use for the batch job.
     * @param request The creation request payload.
     * @return The created [BatchJob] (Operation).
     */
    suspend fun createBatch(
        model: String,
        request: CreateBatchRequest,
    ): BatchJob {
        val urlString = "$baseUrl/$model:batchGenerateContent"
        return json.decodeFromString<BatchJob>(
            getContent(urlString, json.encodeToString(request)),
        )
    }

    /**
     * Gets the status of a batch job (Operation).
     *
     * @param name The resource name of the batch job operation (e.g., "batches/123456").
     * @return The [BatchJob] with current status.
     */
    suspend fun getBatch(name: String): BatchJob {
        val urlString = "$bUrl/$name"
        return json.decodeFromString<BatchJob>(
            getContent(urlString),
        )
    }

    /**
     * Cancels a batch job.
     *
     * @param name The resource name of the batch job to cancel.
     */
    suspend fun cancelBatch(name: String) {
        val urlString = "$bUrl/$name:cancel"
        getContent(urlString, "{}") // POST with empty body
    }

    /**
     * Deletes a batch job.
     *
     * @param name The resource name of the batch job to delete.
     */
    suspend fun deleteBatch(name: String) {
        val urlString = "$bUrl/$name"
        deleteContent(urlString)
    }

    /**
     * Creates a batch job for creating embeddings.
     *
     * @param model The model to use for the batch job.
     * @param request The creation request payload.
     * @return The created [BatchJob] (Operation).
     */
    suspend fun createBatchEmbeddings(
        model: String,
        request: CreateBatchRequest,
    ): BatchJob {
        val urlString = "$baseUrl/$model:asyncBatchEmbedContent"
        return json.decodeFromString<BatchJob>(
            getContent(urlString, json.encodeToString(request)),
        )
    }

    /**
     * Lists batch jobs.
     *
     * @param pageSize The maximum number of batch jobs to return.
     * @param pageToken A page token, received from a previous list call.
     * @return A list of [BatchJob]s.
     */
    suspend fun listBatches(
        pageSize: Int? = null,
        pageToken: String? = null,
    ): ListBatchesResponse {
        val urlString =
            buildString {
                append("$bUrl/batches")
                val params = mutableListOf<String>()
                if (pageSize != null) params.add("pageSize=$pageSize")
                if (pageToken != null) params.add("pageToken=$pageToken")
                if (params.isNotEmpty()) append("?${params.joinToString("&")}")
            }
        return json.decodeFromString<ListBatchesResponse>(
            getContent(urlString),
        )
    }

    private suspend fun getContent(
        urlStr: String,
        inputJson: String? = null,
    ): String =
        try {
            logger.info { inputJson }
            val response =
                if (inputJson != null) {
                    client.post(urlStr) {
                        header("x-goog-api-key", apiKey)
                        contentType(ContentType.Application.Json)
                        setBody(inputJson)
                    }
                } else {
                    client.get(urlStr) {
                        header("x-goog-api-key", apiKey)
                    }
                }

            if (response.status != HttpStatusCode.OK) {
                logger.error { "Error: ${response.status}" }
                val errorMsg = response.bodyAsText()
                logger.error { "Error Message: $errorMsg" }
                try {
                    val errorResponse = json.decodeFromString<GeminiErrorResponse>(errorMsg)
                    throw GeminiException(errorResponse.error)
                } catch (e: GeminiException) {
                    throw e
                } catch (e: SerializationException) {
                    logger.error { "Failed to parse error message: ${e.message}" }
                } catch (e: IllegalArgumentException) {
                    logger.error { "Failed to parse error message: ${e.message}" }
                }
                "{}"
            } else {
                logger.info { "GenerateContentResponse Code: ${response.status}" }
                val txt = response.bodyAsText()
                logger.debug { "Content length: ${txt.length}" }
                logger.debug { "Content preview: ${txt.take(PREVIEW_LENGTH)}" }
                logger.debug { txt }
                txt
            }
        } catch (e: IOException) {
            logger.error { e.stackTraceToString() }
            ""
        } catch (e: Exception) {
            logger.error { e.stackTraceToString() }
            throw e
        }

    private suspend fun deleteContent(urlStr: String) {
        try {
            val response =
                client.delete(urlStr) {
                    header("x-goog-api-key", apiKey)
                }
            if (response.status != HttpStatusCode.OK) {
                logger.error { "Error: ${response.status}" }
                val errorMsg = response.bodyAsText()
                logger.error { "Error Message: $errorMsg" }
            }
            logger.info { "GenerateContentResponse Code: ${response.status}" }
        } catch (e: IOException) {
            logger.error { e.stackTraceToString() }
        } catch (e: Exception) {
            logger.error { e.stackTraceToString() }
        }
    }
}
