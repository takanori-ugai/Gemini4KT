package io.github.ugaikit.gemini4kt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.ugaikit.gemini4kt.live.GeminiLive
import io.github.ugaikit.gemini4kt.live.LiveConnectConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A logger for logging messages. Uses KotlinLogging library for simplified logging.
 */
private val logger = KotlinLogging.logger {}

/**
 * Represents a client for interacting with the Gemini API, providing methods to extract content,
 * embed content, and retrieve model information.
 *
 * @property apiKey The API key used for authenticating requests to the Gemini API.
 */
@Suppress("TooManyFunctions")
class Gemini(
    private val apiKey: String,
    private val client: HttpClient? = null,
) {
    /**
     * JSON configuration setup to ignore unknown keys during deserialization.
     */
    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = client ?: createHttpClient(json)
    private val fileUploadProvider: FileUploadProvider = FileUploadProviderImpl(apiKey, httpClient, json)

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
    suspend fun generateContent(
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
            try {
                val response =
                    httpClient.post(urlString) {
                        header("x-goog-api-key", apiKey)
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString<GenerateContentRequest>(inputJson))
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
                } else {
                    val channel = response.bodyAsChannel()
                    while (!channel.isClosedForRead) {
                        val line = channel.readUTF8Line() ?: break
                        if (line.startsWith("data: ")) {
                            val jsonStr = line.substring(6)
                            try {
                                val result = json.decodeFromString<GenerateContentResponse>(jsonStr)
                                emit(result)
                            } catch (e: SerializationException) {
                                logger.error { "Failed to parse stream response: ${e.message}" }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error { "Stream error: ${e.message}" }
                throw e
            }
        }

    /**
     * Creates a new cached content entry in the system.
     *
     * @param inputJson The [CachedContent] object to be created.
     * @return The created [CachedContent] object as returned by the server.
     */
    suspend fun createCachedContent(inputJson: CachedContent): CachedContent {
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
    suspend fun listCachedContent(
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
    suspend fun getCachedContent(name: String): CachedContent {
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
    suspend fun deleteCachedContent(name: String) {
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
    suspend fun countTokens(
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
    suspend fun batchEmbedContents(
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
    suspend fun embedContent(
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
    suspend fun getModels(): ModelCollection {
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
        file: PlatformFile,
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
    suspend fun getContent(
        urlStr: String,
        inputJson: String? = null,
    ): String =
        try {
            logger.info { inputJson }
            val response: HttpResponse =
                if (inputJson == null) {
                    httpClient.get(urlStr) {
                        header("x-goog-api-key", apiKey)
                        header("Content-Type", "application/json")
                    }
                } else {
                    httpClient.post(urlStr) {
                        header("x-goog-api-key", apiKey)
                        contentType(ContentType.Application.Json)
                        setBody(inputJson)
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
                logger.trace { "Content : $txt" }
                txt
            }
        } catch (e: ClientRequestException) {
            // Catch Ktor specific exceptions if needed
            logger.error { "Client Request Exception: ${e.message}" }
            throw e
        }

    /**
     * Sends a DELETE request to the specified URL to delete content.
     *
     * @param urlStr The URL string where the DELETE request is sent.
     */
    suspend fun deleteContent(urlStr: String) {
        try {
            val response =
                httpClient.delete(urlStr) {
                    header("x-goog-api-key", apiKey)
                }
            if (response.status != HttpStatusCode.OK) {
                logger.error { "Error: ${response.status}" }
                val msg = response.bodyAsText()
                logger.error { "Error Message: $msg" }
            }
            logger.info { "GenerateContentResponse Code: ${response.status}" }
        } catch (e: Exception) {
            logger.error { e.message }
        }
    }
}
