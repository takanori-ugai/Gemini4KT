package io.github.ugaikit.gemini4kt

/**
 * Validates that a credential value exists and is not blank.
 *
 * @param value The credential value to validate.
 * @param source A short description of where the credential came from.
 * @return The non-blank credential value.
 */
internal fun requireNonBlankCredential(
    value: String?,
    source: String,
): String =
    value?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException("$source is missing or blank. Provide a non-blank apiKey.")

/**
 * Represents the initial payload for file upload metadata.
 */
@kotlinx.serialization.Serializable
internal data class UploadFileRequest(
    val file: UploadFileRequestFile,
)

/**
 * Represents the file metadata for an upload request.
 */
@kotlinx.serialization.Serializable
internal data class UploadFileRequestFile(
    val displayName: String,
)
