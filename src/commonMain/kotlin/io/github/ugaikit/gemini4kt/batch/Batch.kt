package io.github.ugaikit.gemini4kt.batch

import io.github.ugaikit.gemini4kt.buildModelUrl
import io.github.ugaikit.gemini4kt.buildUrl
import io.github.ugaikit.gemini4kt.createHttpClient
import io.github.ugaikit.gemini4kt.normalizeResourcePathSegments
import io.github.ugaikit.gemini4kt.throwApiException
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Client for interacting with the Gemini Batch API.
 *
 * @property apiKey The API key used for authenticating requests.
 */
class Batch(
    private val apiKey: String,
    private val client: HttpClient? = null,
) {
    init {
        require(apiKey.isNotBlank()) { "apiKey must not be blank." }
    }

    /**
     * Holds the json.
     */
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Holds the http client.
     */
    private val httpClient = client ?: createHttpClient(json)

    /**
     * Tracks whether this instance owns the HTTP client and should close it.
     */
    private val ownsHttpClient = client == null

    /**
     * Holds the b url.
     */
    private val bUrl = "https://generativelanguage.googleapis.com/v1beta"

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
        val urlString = buildModelUrl(bUrl, model, "batchGenerateContent")
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
        val urlString = batchResourceUrl(name)
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
        val urlString = batchResourceUrl(name) + ":cancel"
        getContent(urlString, "{}") // POST with empty body
    }

    /**
     * Deletes a batch job.
     *
     * @param name The resource name of the batch job to delete.
     */
    suspend fun deleteBatch(name: String) {
        val urlString = batchResourceUrl(name)
        deleteContent(urlString)
    }

    private fun batchResourceUrl(name: String): String = buildUrl(bUrl, listOf("batches") + normalizeResourcePathSegments(name, "batches"))

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
        val urlString = buildModelUrl(bUrl, model, "asyncBatchEmbedContent")
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
            buildUrl(
                bUrl,
                listOf("batches"),
                mapOf(
                    "pageSize" to pageSize?.toString(),
                    "pageToken" to pageToken,
                ),
            )
        return json.decodeFromString<ListBatchesResponse>(
            getContent(urlString),
        )
    }

    /**
     * Handles get content.
     *
     * @param urlStr The url str.
     * @param inputJson The input json.
     */
    private suspend fun getContent(
        urlStr: String,
        inputJson: String? = null,
    ): String =
        with(httpClient) {
            val response: HttpResponse =
                if (inputJson == null) {
                    get(urlStr) {
                        header("x-goog-api-key", apiKey)
                        header("Content-Type", "application/json")
                    }
                } else {
                    post(urlStr) {
                        header("x-goog-api-key", apiKey)
                        contentType(ContentType.Application.Json)
                        setBody(inputJson)
                    }
                }

            if (!response.status.isSuccess()) {
                response.throwApiException()
            }
            response.bodyAsText()
        }

    /**
     * Handles delete content.
     *
     * @param urlStr The url str.
     */
    private suspend fun deleteContent(urlStr: String) {
        val response =
            httpClient.delete(urlStr) {
                header("x-goog-api-key", apiKey)
            }
        if (!response.status.isSuccess()) {
            response.throwApiException()
        }
    }

    /**
     * Closes the owned HTTP client, if this instance created one.
     */
    fun close() {
        if (ownsHttpClient) {
            httpClient.close()
        }
    }
}
