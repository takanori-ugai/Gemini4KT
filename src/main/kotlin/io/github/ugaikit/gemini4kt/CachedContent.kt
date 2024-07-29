package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class CachedContent(
    val contents: List<Content>,
    val tools: List<Tool>,
    val createTime: String,
    val updateTime: String,
    val usageMetadata: UsageMetadata,
    val expireTime: String? = null,
    val ttl: String? = null,
    val name: String,
    val displayName: String,
    val model: String,
    val systemInstruction: Content,
    val toolConfig: ToolConfig
)