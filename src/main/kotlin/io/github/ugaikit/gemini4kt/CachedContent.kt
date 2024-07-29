package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class CachedContent(
    val contents: List<Content>,
    val tools: List<Tool>? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val usageMetadata: UsageMetadata? = null,
    val expireTime: String? = null,
    val ttl: String? = null,
    val name: String? = null,
    val displayName: String? = null,
    val model: String = "gemini-1.5-flash-001",
    val systemInstruction: Content,
    val toolConfig: ToolConfig? = null,
)
