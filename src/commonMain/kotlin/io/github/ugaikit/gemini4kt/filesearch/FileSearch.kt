package io.github.ugaikit.gemini4kt.filesearch

import io.github.ugaikit.gemini4kt.FileUploadProvider
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
import kotlinx.io.files.Path
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Client for interacting with the Google File Search API.
 *
 * @property apiKey The API key used for authenticating requests.
 */
class FileSearch(
    private val apiKey: String,
    private val client: HttpClient? = null,
    uploadProvider: FileUploadProvider? = null,
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
     * Holds the file upload provider.
     */
    private val fileUploadProvider: FileUploadProvider =
        uploadProvider ?: FileUploadProvider(apiKey, httpClient)

    /**
     * Holds the b url.
     */
    private val bUrl = "https://generativelanguage.googleapis.com/v1beta"

    /**
     * Creates a new FileSearchStore.
     *
     * @param inputJson The request payload for creating a FileSearchStore.
     * @return The created [FileSearchStore] object.
     */
    suspend fun createFileSearchStore(inputJson: FileSearchStore): FileSearchStore {
        val urlString = buildUrl(bUrl, listOf("fileSearchStores"))
        return json.decodeFromString<FileSearchStore>(
            getContent(urlString, json.encodeToString(inputJson)),
        )
    }

    /**
     * Gets information about a specific FileSearchStore.
     *
     * @param name The name of the FileSearchStore.
     * @return The [FileSearchStore] object.
     */
    suspend fun getFileSearchStore(name: String): FileSearchStore {
        val urlString = buildUrl(bUrl, listOf("fileSearchStores") + normalizeResourcePathSegments(name, "fileSearchStores"))
        return json.decodeFromString<FileSearchStore>(
            getContent(urlString),
        )
    }

    /**
     * Lists all FileSearchStores owned by the user.
     *
     * @param pageSize The maximum number of FileSearchStores to return.
     * @param pageToken A page token, received from a previous fileSearchStores.list call.
     * @return A [ListFileSearchStoresResponse] containing the list of FileSearchStores.
     */
    suspend fun listFileSearchStores(
        pageSize: Int? = null,
        pageToken: String? = null,
    ): ListFileSearchStoresResponse {
        val urlString =
            buildUrl(
                bUrl,
                listOf("fileSearchStores"),
                mapOf(
                    "pageSize" to pageSize?.toString(),
                    "pageToken" to pageToken,
                ),
            )
        return json.decodeFromString<ListFileSearchStoresResponse>(
            getContent(urlString),
        )
    }

    /**
     * Deletes a FileSearchStore.
     *
     * @param name The name of the FileSearchStore.
     * @param force If set to true, any Documents and objects related to this FileSearchStore will also be deleted.
     */
    suspend fun deleteFileSearchStore(
        name: String,
        force: Boolean = false,
    ) {
        val urlString =
            buildUrl(
                bUrl,
                listOf("fileSearchStores") + normalizeResourcePathSegments(name, "fileSearchStores"),
                mapOf("force" to if (force) "true" else null),
            )
        deleteContent(urlString)
    }

    /**
     * Imports a File from File Service to a FileSearchStore.
     *
     * @param fileSearchStoreName The name of the FileSearchStore.
     * @param inputJson The request payload for importing a file.
     * @return The [Operation] object.
     */
    suspend fun importFileToFileSearchStore(
        fileSearchStoreName: String,
        inputJson: ImportFileRequest,
    ): Operation {
        val urlString = buildUrl(bUrl, listOf("fileSearchStores") + normalizeResourcePathSegments(fileSearchStoreName, "fileSearchStores")) + ":importFile"
        return json.decodeFromString<Operation>(
            getContent(urlString, json.encodeToString(inputJson)),
        )
    }

    /**
     * Uploads a file to a FileSearchStore.
     *
     * @param fileSearchStoreName The name of the FileSearchStore.
     * @param file The file to upload.
     * @param mimeType The MIME type of the file.
     * @param uploadRequest The request payload for uploading a file.
     * @return The [Operation] object.
     */
    suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: Path,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation = fileUploadProvider.uploadToFileSearchStore(fileSearchStoreName, file, mimeType, uploadRequest)

    /**
     * Gets the latest state of a long-running operation.
     *
     * @param name The name of the operation resource.
     * @return The [Operation] object.
     */
    suspend fun getFileSearchStoreOperation(name: String): Operation {
        val urlString = buildUrl(bUrl, listOf("operations") + normalizeResourcePathSegments(name, "operations"))
        return json.decodeFromString<Operation>(
            getContent(urlString),
        )
    }

    /**
     * Performs a POST request to the specified URL string with the given input JSON payload.
     *
     * @param urlStr The URL to which the POST request is made.
     * @param inputJson The JSON payload for the request.
     * @return The response body as a String.
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
     * Sends a DELETE request to the specified URL to delete content.
     *
     * @param urlStr The URL string where the DELETE request is sent.
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
