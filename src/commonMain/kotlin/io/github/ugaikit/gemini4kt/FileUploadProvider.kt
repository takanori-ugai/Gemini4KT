package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.filesearch.Operation
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest

expect class PlatformFile

interface FileUploadProvider {
    suspend fun upload(
        file: PlatformFile,
        mimeType: String,
        displayName: String,
    ): GeminiFile

    suspend fun uploadToFileSearchStore(
        fileSearchStoreName: String,
        file: PlatformFile,
        mimeType: String,
        uploadRequest: UploadFileSearchStoreRequest,
    ): Operation
}
