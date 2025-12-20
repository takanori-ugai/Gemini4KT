package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the gemini file.
 *
 * @property name The name.
 * @property displayName The display name.
 * @property uri The uri.
 * @property mimeType The mime type.
 * @property createTime The create time.
 * @property updateTime The update time.
 * @property expirationTime The expiration time.
 * @property sha256Hash The sha256 hash.
 * @property sizeBytes The size bytes.
 */
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
    val sizeBytes: Long,
)
