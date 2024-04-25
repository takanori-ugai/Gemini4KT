package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class FileData(
    val mimeType: String,
    val fileUri: String,
)
