package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import kotlinx.io.files.Path

interface FileUploadProvider {
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
