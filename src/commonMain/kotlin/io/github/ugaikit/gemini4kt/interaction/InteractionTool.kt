package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionTool(
    val type: String,
    val name: String? = null,
    val description: String? = null,
    val parameters: JsonElement? = null,
    val environment: String? = null,
    val excludedPredefinedFunctions: Array<String>? = null,
    val url: String? = null,
    val headers: JsonElement? = null,
    @SerialName("allowed_tools") val allowedTools: Array<InteractionAllowedTools>? = null,
    @SerialName("file_search_store_names") val fileSearchStoreNames: Array<String>? = null,
    @SerialName("top_k") val topK: Int? = null,
    @SerialName("metadata_filter") val metadataFilter: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InteractionTool

        if (type != other.type) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (parameters != other.parameters) return false
        if (environment != other.environment) return false
        if (excludedPredefinedFunctions != null) {
            if (other.excludedPredefinedFunctions == null) return false
            if (!excludedPredefinedFunctions.contentEquals(other.excludedPredefinedFunctions)) return false
        } else if (other.excludedPredefinedFunctions != null) {
            return false
        }
        if (url != other.url) return false
        if (headers != other.headers) return false
        if (allowedTools != null) {
            if (other.allowedTools == null) return false
            if (!allowedTools.contentEquals(other.allowedTools)) return false
        } else if (other.allowedTools != null) {
            return false
        }
        if (fileSearchStoreNames != null) {
            if (other.fileSearchStoreNames == null) return false
            if (!fileSearchStoreNames.contentEquals(other.fileSearchStoreNames)) return false
        } else if (other.fileSearchStoreNames != null) {
            return false
        }
        if (topK != other.topK) return false
        if (metadataFilter != other.metadataFilter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (parameters?.hashCode() ?: 0)
        result = 31 * result + (environment?.hashCode() ?: 0)
        result = 31 * result + (excludedPredefinedFunctions?.contentHashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (headers?.hashCode() ?: 0)
        result = 31 * result + (allowedTools?.contentHashCode() ?: 0)
        result = 31 * result + (fileSearchStoreNames?.contentHashCode() ?: 0)
        result = 31 * result + (topK ?: 0)
        result = 31 * result + (metadataFilter?.hashCode() ?: 0)
        return result
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionAllowedTools(
    val mode: String? = null,
    val tools: Array<String>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InteractionAllowedTools

        if (mode != other.mode) return false
        if (tools != null) {
            if (other.tools == null) return false
            if (!tools.contentEquals(other.tools)) return false
        } else if (other.tools != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = mode?.hashCode() ?: 0
        result = 31 * result + (tools?.contentHashCode() ?: 0)
        return result
    }
}
