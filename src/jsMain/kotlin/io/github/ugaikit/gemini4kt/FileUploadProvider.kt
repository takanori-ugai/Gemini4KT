package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.HttpClient
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

/**
 * Represents the file wrapper.
 *
 * @property file The file.
 */
@kotlinx.serialization.Serializable
private data class FileWrapper(
    val file: GeminiFile,
)

/**
 * Represents the file upload provider.
 *
 * @property apiKey The api key.
 * @property client The client.
 * @property json The json.
 */
actual class FileUploadProvider actual constructor(
    private val apiKey: String,
    private val client: HttpClient?,
    private val json: Json,
) {
    init {
        require(apiKey.isNotBlank()) { "apiKey must not be blank." }
    }

    /**
     * Holds the http client.
     */
    private val httpClient = client ?: createHttpClient(json)

    /**
     * Holds the fs.
     */
    private val fs: dynamic by lazy { loadNodeFs() }

    /**
     * Handles upload.
     *
     * @param file The file.
     * @param mimeType The mime type.
     * @param displayName The display name.
     */
    actual suspend fun upload(
        file: Path,
        mimeType: String,
        displayName: String,
    ): GeminiFile {
        val pathStr = file.toString()
        val fileSize = getFileSize(pathStr)
        val requestBody = json.encodeToString(UploadFileRequest(UploadFileRequestFile(displayName)))
        val fileContent = readFile(pathStr)
        return httpClient
            .performResumableUploadAndDecode<FileWrapper>(
                apiKey = apiKey,
                mimeType = mimeType,
                fileSize = fileSize,
                startBody = requestBody,
                uploadBody = fileContent,
                json = json,
            ).file
    }

    /**
     * Handles upload to file search store.
     *
     * @param fileSearchStoreName The file search store name.
     * @param file The file.
     * @param mimeType The mime type.
     * @param uploadRequest The upload request.
     */
    actual suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: Path,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation {
        val pathStr = file.toString()
        val fileSize = getFileSize(pathStr)
        val requestBody = json.encodeToString(uploadRequest)
        val fileContent = readFile(pathStr)
        return httpClient
            .performResumableUploadAndDecode<Operation>(
                apiKey = apiKey,
                mimeType = mimeType,
                fileSize = fileSize,
                startBody = requestBody,
                uploadBody = fileContent,
                json = json,
                endpoint = "upload/v1beta/$fileSearchStoreName:uploadToFileSearchStore",
            )
    }

    /**
     * Handles get file size.
     *
     * @param path The path.
     */
    private fun getFileSize(path: String): Long {
        try {
            val stats = fs.statSync(path)
            return (stats.size as Number).toLong()
        } catch (e: dynamic) {
            throw IOException("Failed to get file size for $path: $e")
        }
    }

    /**
     * Handles read file.
     *
     * @param path The path.
     */
    private fun readFile(path: String): ByteArray {
        try {
            val buffer = fs.readFileSync(path)
            val uint8Array = buffer.unsafeCast<Uint8Array>()
            val int8Array = Int8Array(uint8Array.buffer, uint8Array.byteOffset, uint8Array.length)
            return int8Array.unsafeCast<ByteArray>()
        } catch (e: dynamic) {
            throw IOException("Failed to read file $path: $e")
        }
    }

    /**
     * Loads the Node.js fs module when available.
     */
    private fun loadNodeFs(): dynamic {
        val module =
            js(
                """(function() {
                  if (typeof process === 'undefined' || process == null || !process.versions || !process.versions.node) {
                    return null;
                  }
                  if (typeof require === 'function') {
                    return require('node:fs');
                  }
                  if (typeof module !== 'undefined' && typeof module.require === 'function') {
                    return module.require('node:fs');
                  }
                  return null;
                })()""",
            )
        if (module == null) {
            throw IOException("File upload is only supported in a Node.js environment.")
        }
        return module
    }
}
