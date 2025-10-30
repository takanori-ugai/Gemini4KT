package io.github.ugaikit.gemini4kt

import java.io.File

interface FileUploadProvider {
    suspend fun upload(file: File, mimeType: String, displayName: String): GeminiFile
}
