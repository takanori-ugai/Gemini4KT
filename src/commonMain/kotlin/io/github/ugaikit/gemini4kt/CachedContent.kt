package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
@JsExport
data class CachedContent(
    val contents: Array<Content>? = null,
    val tools: Array<Tool>? = null,
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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CachedContent

        if (contents != null) {
            if (other.contents == null) return false
            if (!contents.contentEquals(other.contents)) return false
        } else if (other.contents != null) return false
        if (tools != null) {
            if (other.tools == null) return false
            if (!tools.contentEquals(other.tools)) return false
        } else if (other.tools != null) return false
        if (createTime != other.createTime) return false
        if (updateTime != other.updateTime) return false
        if (usageMetadata != other.usageMetadata) return false
        if (expireTime != other.expireTime) return false
        if (ttl != other.ttl) return false
        if (name != other.name) return false
        if (displayName != other.displayName) return false
        if (model != other.model) return false
        if (systemInstruction != other.systemInstruction) return false
        if (toolConfig != other.toolConfig) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contents?.contentHashCode() ?: 0
        result = 31 * result + (tools?.contentHashCode() ?: 0)
        result = 31 * result + (createTime?.hashCode() ?: 0)
        result = 31 * result + (updateTime?.hashCode() ?: 0)
        result = 31 * result + (usageMetadata?.hashCode() ?: 0)
        result = 31 * result + (expireTime?.hashCode() ?: 0)
        result = 31 * result + (ttl?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (displayName?.hashCode() ?: 0)
        result = 31 * result + (model?.hashCode() ?: 0)
        result = 31 * result + (systemInstruction?.hashCode() ?: 0)
        result = 31 * result + (toolConfig?.hashCode() ?: 0)
        return result
    }
}
