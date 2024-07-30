package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

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
