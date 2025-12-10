package io.github.ugaikit.gemini4kt.batch

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.ugaikit.gemini4kt.DefaultHttpConnectionProvider
import io.github.ugaikit.gemini4kt.GeminiErrorResponse
import io.github.ugaikit.gemini4kt.GeminiException
import io.github.ugaikit.gemini4kt.HttpConnectionProvider
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.URL

private val logger = KotlinLogging.logger {}

/**
 * Client for interacting with the Gemini Batch API.
 *
 * @property apiKey The API key used for authenticating requests.
 */
class Batch(
    private val apiKey: String,
    private val httpConnectionProvider: HttpConnectionProvider = DefaultHttpConnectionProvider(),
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val bUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val baseUrl = "$bUrl/models"

    companion object {
        private const val HTTP_OK = 200
        private const val PREVIEW_LENGTH = 100
    }

    /**
     * Creates a batch job for content generation.
     *
     * @param model The model to use for the batch job.
     * @param request The creation request payload.
     * @return The created [BatchJob] (Operation).
     */
    fun createBatch(
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
    fun getBatch(name: String): BatchJob {
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
    fun cancelBatch(name: String) {
        val urlString = "$bUrl/$name:cancel"
        getContent(urlString, "{}") // POST with empty body
    }

    /**
     * Deletes a batch job.
     *
     * @param name The resource name of the batch job to delete.
     */
    fun deleteBatch(name: String) {
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
    fun createBatchEmbeddings(
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
    fun listBatches(
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

    private fun getContent(
        urlStr: String,
        inputJson: String? = null,
    ): String =
        try {
            logger.info { inputJson }
            val url = URL(urlStr)
            val conn = httpConnectionProvider.getConnection(url)
            conn.requestMethod = if (inputJson == null) "GET" else "POST"
            conn.setRequestProperty("x-goog-api-key", apiKey)
            conn.setRequestProperty("Content-Type", "application/json")
            if (inputJson != null) {
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream).use { writer -> writer.write(inputJson) }
            }
            val resCode = conn.responseCode
            if (resCode != HTTP_OK) {
                logger.error { "Error: ${conn.responseCode}" }
                val errorMsg = conn.errorStream?.bufferedReader()?.use { it.readText() }
                if (errorMsg != null) {
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
                }
                "{ \"name\" : \"Error\" }"
            } else {
                logger.info { "GenerateContentResponse Code: $resCode" }
                conn.inputStream.bufferedReader().use { reader ->
                    val txt = reader.readText()
                    logger.debug { "Content length: ${txt.length}" }
                    logger.debug { "Content preview: ${txt.take(PREVIEW_LENGTH)}" }
                    logger.debug { txt }
                    txt
                }
            }
        } catch (e: IOException) {
            logger.error { e.stackTrace.contentToString() }
            ""
        }

    private fun deleteContent(urlStr: String) {
        try {
            val url = URL(urlStr)
            val conn = httpConnectionProvider.getConnection(url)
            conn.requestMethod = "DELETE"
            conn.setRequestProperty("x-goog-api-key", apiKey)
            val resCode = conn.responseCode
            if (resCode != HTTP_OK) {
                logger.error { "Error: $resCode" }
                conn.errorStream?.bufferedReader()?.use { reader ->
                    logger.error { "Error Message: ${reader.readText()}" }
                }
            }
            logger.info { "GenerateContentResponse Code: $resCode" }
        } catch (e: IOException) {
            logger.error { e.stackTrace.contentToString() }
        }
    }
}
