package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the cached content.
 *
 * @property contents The contents.
 * @property tools The tools.
 * @property createTime The create time.
 * @property updateTime The update time.
 * @property usageMetadata The usage metadata.
 * @property expireTime The expire time.
 * @property ttl The ttl.
 * @property name The name.
 * @property displayName The display name.
 * @property model The model.
 * @property systemInstruction The system instruction.
 * @property toolConfig The tool config.
 */
@Serializable
data class CachedContent(
    val contents: List<Content>? = null,
    val tools: List<Tool>? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val usageMetadata: UsageMetadata? = null,
    val expireTime: String? = null,
    val ttl: String? = null,
    val name: String? = null,
    val displayName: String? = null,
    val model: String? = null,
    val systemInstruction: Content? = null,
    val toolConfig: ToolConfig? = null,
)
