package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import java.io.File

interface FileUploadProvider {
    suspend fun upload(
        file: File,
        mimeType: String,
        displayName: String,
    ): GeminiFile

    suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: File,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation
}
