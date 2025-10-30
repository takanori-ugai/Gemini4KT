package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class GeminiFile(
    val name: String,
    val displayName: String,
    val uri: String,
    val mimeType: String,
    val createTime: String,
    val updateTime: String,
    val expirationTime: String,
    val sha256Hash: String,
    val sizeBytes: Long
)
