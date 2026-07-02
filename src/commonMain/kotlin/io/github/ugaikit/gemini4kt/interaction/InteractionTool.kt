package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Tool descriptor used by the Interaction API.
 *
 * @property type Tool type identifier.
 * @property name Optional tool name.
 * @property description Optional tool description.
 * @property parameters Optional JSON schema-like parameter definition.
 * @property environment Optional environment target name.
 * @property excludedPredefinedFunctions Optional predefined function names to exclude.
 * @property url Optional endpoint URL for remote tools.
 * @property headers Optional request headers for remote tools.
 * @property allowedTools Optional allowlist and mode for tool usage.
 * @property fileSearchStoreNames Optional file search stores associated with this tool.
 * @property topK Optional top-k retrieval value.
 * @property metadataFilter Optional metadata filter expression.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionTool(
    val type: String,
    val name: String? = null,
    val description: String? = null,
    val parameters: JsonElement? = null,
    val environment: String? = null,
    @SerialName("excluded_predefined_functions")
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
        if (!excludedPredefinedFunctions.contentEquals(other.excludedPredefinedFunctions)) return false
        if (url != other.url) return false
        if (headers != other.headers) return false
        if (!allowedTools.contentEquals(other.allowedTools)) return false
        if (!fileSearchStoreNames.contentEquals(other.fileSearchStoreNames)) return false
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

/**
 * Tool allowlist configuration.
 *
 * @property mode Optional allowlist mode.
 * @property tools Optional tool-name allowlist.
 */
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
        if (!tools.contentEquals(other.tools)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mode?.hashCode() ?: 0
        result = 31 * result + (tools?.contentHashCode() ?: 0)
        return result
    }
}
