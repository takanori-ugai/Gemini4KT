package io.github.ugaikit.gemini4kt

import kotlinx.io.files.Path
import java.io.File

/**
 * Uploads a file to the Gemini API.
 *
 * @param file The file to upload.
 * @param mimeType The MIME type of the file.
 * @param displayName The display name of the file.
 * @return The uploaded file as a [GeminiFile] object.
 */
suspend fun Gemini.uploadFile(
    file: File,
    mimeType: String,
    displayName: String,
): GeminiFile = uploadFile(Path(file.path), mimeType, displayName)
