package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.ktor.client.HttpClient
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

expect class FileUploadProvider(
    apiKey: String,
    client: HttpClient? = null,
    json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun upload(
        file: Path,
        mimeType: String,
        displayName: String,
    ): GeminiFile

    suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: Path,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation
}
