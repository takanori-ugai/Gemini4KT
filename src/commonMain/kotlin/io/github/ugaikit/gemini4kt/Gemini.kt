package io.github.ugaikit.gemini4kt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
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
import kotlinx.coroutines.flow.flow
import kotlinx.io.files.Path
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.JsName

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
    internal val apiKey: String,
    private val client: HttpClient? = null,
    private val fileUploadProvider: FileUploadProvider? = null,
) {
    init {
        require(apiKey.isNotBlank()) { "apiKey must not be blank." }
    }

    /**
     * Executes Gemini function-calling turns automatically until the model
     * returns a non-function-call response.
     *
     * The first candidate is used for function-call extraction on each turn.
     *
     * @param request Initial request containing user contents and tool declarations.
     * @param functionHandlers Map of function name to handler implementation.
     * @param model The model to use.
     * @param maxIterations Maximum number of tool turns before failing.
     * @return Final [GenerateContentResponse] produced by the model.
     * @throws IllegalArgumentException If `maxIterations` is not positive or a handler is missing.
     * @throws IllegalStateException If function-calling does not finish within `maxIterations`.
     */
    suspend fun generateContent(
        request: GenerateContentRequest,
        functionHandlers: Map<String, suspend (FunctionCall) -> FunctionResponse>,
        model: String = "gemini-flash-lite-latest",
        maxIterations: Int = 8,
    ): GenerateContentResponse {
        require(maxIterations > 0) { "maxIterations must be greater than 0." }

        var currentRequest = request
        repeat(maxIterations) {
            val response = generateContent(currentRequest, model)
            val candidate = response.candidates.firstOrNull() ?: return response
            val modelParts: Array<Part> = candidate.content.parts ?: emptyArray()
            val functionCalls = modelParts.mapNotNull { it.functionCall }

            if (functionCalls.isEmpty()) {
                return response
            }

            val functionResponseParts =
                functionCalls.map { functionCall ->
                    val handler =
                        functionHandlers[functionCall.name]
                            ?: throw IllegalArgumentException(
                                "No function handler registered for '${functionCall.name}'.",
                            )
                    val functionResponse = handler(functionCall)
                    Part(functionResponse = functionResponse)
                }

            val modelContent = Content(role = "model", parts = modelParts)
            val functionContent =
                Content(
                    role = "function",
                    parts = functionResponseParts.toTypedArray(),
                )

            currentRequest =
                currentRequest.copy(
                    contents = currentRequest.contents + arrayOf(modelContent, functionContent),
                )
        }

        throw IllegalStateException(
            "Automatic function calling exceeded maxIterations=$maxIterations.",
        )
    }

    /**
     * JSON configuration setup to ignore unknown keys during deserialization.
     */
    internal val json = Json { ignoreUnknownKeys = true }

    /**
     * Holds the http client.
     */
    private val httpClient = client ?: createHttpClient(json)

    /**
     * Tracks whether this instance owns the HTTP client and should close it.
     */
    private val ownsHttpClient = client == null

    /**
     * Holds the provider.
     */
    private val provider: FileUploadProvider = fileUploadProvider ?: FileUploadProvider(apiKey, httpClient, json)

    /**
     * Holds the b url.
     */
    private val bUrl = "https://generativelanguage.googleapis.com/v1beta"

    /**
     * Holds the base url.
     */
    private val baseUrl = "$bUrl/models"

    /**
     * Generates content based on the provided input JSON using a specified model.
     *
     * @param inputJson The request payload for content generation.
     * @param model The model to be used for content generation. Defaults to "gemini-flash-lite-latest".
     * @return The response from the Gemini API as a [GenerateContentResponse] object.
     */
    suspend fun generateContent(
        inputJson: GenerateContentRequest,
        model: String = "gemini-flash-lite-latest",
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
     * @param model The model to be used for content generation. Defaults to "gemini-flash-lite-latest".
     * @return The response from the Gemini API as a [Flow] of [GenerateContentResponse] object.
     */
    fun streamGenerateContent(
        inputJson: GenerateContentRequest,
        model: String = "gemini-flash-lite-latest",
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

                if (!response.status.isSuccess()) {
                    response.throwApiException()
                } else {
                    val channel = response.bodyAsChannel()
                    channel.consumeServerSentEvents { jsonStr ->
                        if (jsonStr == "[DONE]") return@consumeServerSentEvents
                        try {
                            val result = json.decodeFromString<GenerateContentResponse>(jsonStr)
                            emit(result)
                        } catch (e: SerializationException) {
                            logger.error { "Failed to parse stream response: ${e.message}" }
                        }
                    }
                }
            } catch (e: Exception) {
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
            URLBuilder(bUrl)
                .apply {
                    appendPathSegments("cachedContents")
                    parameters.append("pageSize", pageSize.toString())
                    if (pageToken != null) {
                        parameters.append("pageToken", pageToken)
                    }
                }.buildString()
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
        val urlString =
            URLBuilder(bUrl)
                .apply {
                    appendPathSegments(name)
                }.buildString()
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
        val urlString =
            URLBuilder(bUrl)
                .apply {
                    appendPathSegments(name)
                }.buildString()
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
     * @return The uploaded file wrapped as a [GeminiFile].
     */
    suspend fun uploadFile(
        file: Path,
        mimeType: String,
        displayName: String,
    ): GeminiFile = provider.upload(file, mimeType, displayName)

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
     * Sends a DELETE request to the specified URL to delete content.
     *
     * @param urlStr The URL string where the DELETE request is sent.
     */
    suspend fun deleteContent(urlStr: String) {
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

/**
 * Represents the gemini js export.
 */
@JsName("Gemini")
class GeminiJsExport(
    apiKey: String,
) {
    /**
     * Holds the delegate.
     */
    private val delegate = Gemini(apiKey)

    /**
     * Holds the json helper.
     */
    private val jsonHelper = Json { ignoreUnknownKeys = true }

    /**
     * Handles generate content.
     *
     * @param request The request.
     * @param model The model.
     */
    suspend fun generateContent(
        request: GenerateContentRequest,
        model: String = "gemini-flash-lite-latest",
    ): String {
        val response = delegate.generateContent(request, model)
        return jsonHelper.encodeToString(response)
    }

    /**
     * Releases the wrapped Gemini client.
     */
    fun close() {
        delegate.close()
    }
}
