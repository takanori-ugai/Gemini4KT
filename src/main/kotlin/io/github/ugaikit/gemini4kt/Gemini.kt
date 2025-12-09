package io.github.ugaikit.gemini4kt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.ugaikit.gemini4kt.live.GeminiLive
import io.github.ugaikit.gemini4kt.live.LiveConnectConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * A logger for logging messages. Uses KotlinLogging library for simplified logging.
 */
private val logger = KotlinLogging.logger {}

interface HttpConnectionProvider {
    fun getConnection(url: URL): HttpURLConnection
}

internal class DefaultHttpConnectionProvider : HttpConnectionProvider {
    override fun getConnection(url: URL): HttpURLConnection = url.openConnection() as HttpURLConnection
}

/**
 * Represents a client for interacting with the Gemini API, providing methods to extract content,
 * embed content, and retrieve model information.
 *
 * @property apiKey The API key used for authenticating requests to the Gemini API.
 */
@Suppress("TooManyFunctions")
class Gemini(
    private val apiKey: String,
    private val httpConnectionProvider: HttpConnectionProvider = DefaultHttpConnectionProvider(),
    private val fileUploadProvider: FileUploadProvider = FileUploadProviderImpl(apiKey),
) {
    /**
     * JSON configuration setup to ignore unknown keys during deserialization.
     */
    private val json = Json { ignoreUnknownKeys = true }
    private val bUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val baseUrl = "$bUrl/models"

    companion object {
        private const val HTTP_OK = 200
        private const val PREVIEW_LENGTH = 100
    }

    /**
     * Generates content based on the provided input JSON using a specified model.
     *
     * @param inputJson The request payload for content generation.
     * @param model The model to be used for content generation. Defaults to "gemini-pro".
     * @return The response from the Gemini API as a [GenerateContentResponse] object.
     */
    fun generateContent(
        inputJson: GenerateContentRequest,
        model: String = "gemini-pro",
    ): GenerateContentResponse {
        val urlString = "$baseUrl/$model:generateContent"
        return json.decodeFromString<GenerateContentResponse>(
            getContent(urlString, json.encodeToString<GenerateContentRequest>(inputJson)),
        )
    }

    /**
     * Generates content stream based on the provided input JSON using a specified model.
     *
     * @param inputJson The request payload for content generation.
     * @param model The model to be used for content generation. Defaults to "gemini-pro".
     * @return The response from the Gemini API as a [Flow] of [GenerateContentResponse] object.
     */
    fun streamGenerateContent(
        inputJson: GenerateContentRequest,
        model: String = "gemini-pro",
    ): Flow<GenerateContentResponse> =
        flow {
            val urlString = "$baseUrl/$model:streamGenerateContent?alt=sse"
            val url = URL(urlString)
            val conn = httpConnectionProvider.getConnection(url)
            conn.requestMethod = "POST"
            conn.setRequestProperty("x-goog-api-key", apiKey)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(json.encodeToString<GenerateContentRequest>(inputJson))
            }

            val resCode = conn.responseCode
            if (resCode != HTTP_OK) {
                logger.error { "Error: ${conn.responseCode}" }
                val errorMsg =
                    conn.errorStream.bufferedReader().use { reader ->
                        val text = reader.readText()
                        logger.error { "Error Message: $text" }
                        text
                    }
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
            } else {
                conn.inputStream.bufferedReader().use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        if (line.startsWith("data: ")) {
                            val jsonStr = line.substring(6)
                            try {
                                val response = json.decodeFromString<GenerateContentResponse>(jsonStr)
                                emit(response)
                            } catch (e: SerializationException) {
                                logger.error { "Failed to parse stream response: ${e.message}" }
                            }
                        }
                        line = reader.readLine()
                    }
                }
            }
        }

    /**
     * Creates a new cached content entry in the system.
     *
     * @param inputJson The [CachedContent] object to be created.
     * @return The created [CachedContent] object as returned by the server.
     */
    fun createCachedContent(inputJson: CachedContent): CachedContent {
        val urlString = "$bUrl/cachedContents"
        return json.decodeFromString<CachedContent>(
            getContent(urlString, json.encodeToString<CachedContent>(inputJson)),
        )
    }

    /**
     * Retrieves a list of cached content entries.
     *
     * @param pageSize The maximum number of entries to return, default is 1000.
     * @param pageToken An optional token for pagination, used to fetch the next
     * set of results.
     * @return A [CachedContentList] containing the list of cached content entries.
     */
    fun listCachedContent(
        pageSize: Int = 1000,
        pageToken: String? = null,
    ): CachedContentList {
        val urlString =
            buildString {
                append("$bUrl/cachedContents?pageSize=$pageSize")
                if (pageToken != null) append("&pageToken=$pageToken")
            }
        return json.decodeFromString<CachedContentList>(
            getContent(urlString),
        )
    }

    /**
     * Fetches cached content by name.
     *
     * @param name The unique name identifier for the cached content.
     * @return A [CachedContentList] containing the cached content matching the
     * given name.
     */
    fun getCachedContent(name: String): CachedContent {
        val urlString = "$bUrl/$name"
        return json.decodeFromString<CachedContent>(
            getContent(urlString),
        )
    }

    /**
     * Deletes a specific cached content entry by name.
     *
     * @param name The unique name identifier of the cached content to be deleted.
     */
    fun deleteCachedContent(name: String) {
        val urlString = "$bUrl/$name"
        deleteContent(urlString)
    }

    /**
     * Counts the number of tokens in the provided text using a specified model.
     *
     * @param inputJson The request object containing the text to analyze.
     * @param model The model to use for counting tokens, default is "gemini-2.0-flash-lite".
     * @return A [TotalTokens] object containing the total number of tokens.
     */
    fun countTokens(
        inputJson: CountTokensRequest,
        model: String = "gemini-2.0-flash-lite",
    ): TotalTokens {
        val urlString = "$baseUrl/$model:countTokens"
        println(inputJson)
        return json.decodeFromString<TotalTokens>(
            getContent(urlString, json.encodeToString<CountTokensRequest>(inputJson)),
        )
    }

    /**
     * Embeds contents in batch using the embedding-001 model.
     *
     * @param inputJson The batch embed request payload.
     * @return The batch embed response as a [BatchEmbedResponse] object.
     */
    fun batchEmbedContents(
        inputJson: BatchEmbedRequest,
        model: String = "embedding-001",
    ): BatchEmbedResponse {
        val urlString = "$baseUrl/$model:batchEmbedContents"
        return json.decodeFromString<BatchEmbedResponse>(
            getContent(urlString, json.encodeToString<BatchEmbedRequest>(inputJson)),
        )
    }

    /**
     * Embeds content using the embedding-001 model.
     *
     * @param inputJson The embed request payload.
     * @return The embed response as an [EmbedResponse] object.
     */
    fun embedContent(
        inputJson: EmbedContentRequest,
        model: String = "embedding-001",
    ): EmbedResponse {
        val urlString = "$baseUrl/$model:embedContent"
        return json.decodeFromString<EmbedResponse>(
            getContent(urlString, json.encodeToString<EmbedContentRequest>(inputJson)),
        )
    }

    /**
     * Retrieves a collection of models available in the Gemini API.
     *
     * @return The collection of models as a [ModelCollection] object.
     */
    fun getModels(): ModelCollection {
        val urlString = "$baseUrl"
        return json.decodeFromString<ModelCollection>(getContent(urlString))
    }

    /**
     * Uploads a file to the Gemini API.
     *
     * @param file The file to upload.
     * @param mimeType The MIME type of the file.
     * @param displayName The display name of the file.
     * @return The uploaded file as a [File] object.
     */
    suspend fun uploadFile(
        file: File,
        mimeType: String,
        displayName: String,
    ): GeminiFile = fileUploadProvider.upload(file, mimeType, displayName)

    /**
     * Creates a client for the Live API.
     *
     * @param model The model to be used for the live session.
     * @param config Optional configuration for the live session.
     * @return A [GeminiLive] client instance.
     */
    fun getLiveClient(
        model: String,
        config: LiveConnectConfig? = null,
    ): GeminiLive = GeminiLive(apiKey, model, config, json)

    /**
     * Performs a POST request to the specified URL string with the given input JSON payload.
     *
     * @param urlStr The URL to which the POST request is made.
     * @param inputJson The JSON payload for the request.
     * @return The response body as a String.
     */
    fun getContent(
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
                val errorMsg =
                    conn.errorStream.bufferedReader().use { reader ->
                        val text = reader.readText()
                        logger.error { "Error Message: $text" }
                        text
                    }
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

    /**
     * Sends a DELETE request to the specified URL to delete content.
     *
     * @param urlStr The URL string where the DELETE request is sent.
     */
    fun deleteContent(urlStr: String) {
        try {
            val url = URL(urlStr)
            val conn = httpConnectionProvider.getConnection(url)
            conn.requestMethod = "DELETE"
            conn.setRequestProperty("x-goog-api-key", apiKey)
            val resCode = conn.responseCode
            if (resCode != HTTP_OK) {
                logger.error { "Error: $resCode" }
                conn.errorStream.bufferedReader().use { reader ->
                    logger.error { "Error Message: ${reader.readText()}" }
                }
            }
            logger.info { "GenerateContentResponse Code: $resCode" }
        } catch (e: IOException) {
            logger.error { e.stackTrace.contentToString() }
        }
    }
}
