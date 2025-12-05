package io.github.ugaikit.gemini4kt.filesearch

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.ugaikit.gemini4kt.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.URL

private val logger = KotlinLogging.logger {}

/**
 * Client for interacting with the Google File Search API.
 *
 * @property apiKey The API key used for authenticating requests.
 */
class FileSearch(
    private val apiKey: String,
    private val httpConnectionProvider: HttpConnectionProvider = DefaultHttpConnectionProvider(),
    private val fileUploadProvider: FileUploadProvider = FileUploadProviderImpl(apiKey),
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val bUrl = "https://generativelanguage.googleapis.com/v1beta"

    companion object {
        private const val HTTP_OK = 200
        private const val PREVIEW_LENGTH = 100
    }

    /**
     * Creates a new FileSearchStore.
     *
     * @param inputJson The request payload for creating a FileSearchStore.
     * @return The created [FileSearchStore] object.
     */
    fun createFileSearchStore(inputJson: FileSearchStore): FileSearchStore {
        val urlString = "$bUrl/fileSearchStores"
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
    fun getFileSearchStore(name: String): FileSearchStore {
        val urlString = "$bUrl/$name"
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
    fun listFileSearchStores(
        pageSize: Int? = null,
        pageToken: String? = null,
    ): ListFileSearchStoresResponse {
        val urlString =
            buildString {
                append("$bUrl/fileSearchStores")
                val params = mutableListOf<String>()
                if (pageSize != null) params.add("pageSize=$pageSize")
                if (pageToken != null) params.add("pageToken=$pageToken")
                if (params.isNotEmpty()) append("?${params.joinToString("&")}")
            }
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
    fun deleteFileSearchStore(
        name: String,
        force: Boolean = false,
    ) {
        val urlString = "$bUrl/$name" + if (force) "?force=true" else ""
        deleteContent(urlString)
    }

    /**
     * Imports a File from File Service to a FileSearchStore.
     *
     * @param fileSearchStoreName The name of the FileSearchStore.
     * @param inputJson The request payload for importing a file.
     * @return The [Operation] object.
     */
    fun importFileToFileSearchStore(
        fileSearchStoreName: String,
        inputJson: ImportFileRequest,
    ): Operation {
        val urlString = "$bUrl/$fileSearchStoreName:importFile"
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
        file: File,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation = fileUploadProvider.uploadToFileSearchStore(fileSearchStoreName, file, mimeType, uploadRequest)

    /**
     * Gets the latest state of a long-running operation.
     *
     * @param name The name of the operation resource.
     * @return The [Operation] object.
     */
    fun getFileSearchStoreOperation(name: String): Operation {
        val urlString = "$bUrl/$name"
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
    private fun deleteContent(urlStr: String) {
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
