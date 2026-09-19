package io.github.ugaikit.gemini4kt.interaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Content item produced or consumed by the Interaction API.
 *
 * @property type Content type identifier.
 * @property text Optional plain text content.
 * @property data Optional inline encoded data.
 * @property uri Optional URI reference for external content.
 * @property mimeType Optional MIME type for `data` or `uri` content.
 * @property processing Video processing mode (`static` or `agentic`).
 * @property resolution Optional resolution descriptor for media content.
 * @property signature Optional signature or integrity metadata.
 * @property summary Optional thought summary content.
 * @property name Optional function or tool name associated with this item.
 * @property arguments Optional JSON arguments for a function/tool call.
 * @property isError Indicates whether this content represents an error.
 * @property result Optional JSON result payload.
 * @property callId Optional tool/function call ID.
 * @property language Optional language tag for code/text content.
 * @property code Optional source code payload.
 * @property urls Optional URL list associated with the content.
 * @property status Optional status value for tool/content execution.
 * @property title Optional title text.
 * @property renderedContent Optional pre-rendered rich content.
 * @property serverName Optional server identifier for remote tool content.
 * @property fileSearchStore Optional file search store name.
 * @property annotations Optional annotations attached to this content.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionContent(
    val type: String? = null,
    val text: String? = null,
    val data: String? = null,
    val uri: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    val processing: InteractionVideoProcessing? = null,
    val resolution: String? = null,
    val signature: String? = null,
    val summary: InteractionThoughtSummary? = null,
    val name: String? = null,
    val arguments: JsonElement? = null,
    @SerialName("is_error") val isError: Boolean? = null,
    val result: JsonElement? = null,
    @SerialName("call_id") val callId: String? = null,
    val language: String? = null,
    val code: String? = null,
    val urls: Array<String>? = null,
    val status: String? = null,
    val title: String? = null,
    @SerialName("rendered_content") val renderedContent: String? = null,
    @SerialName("server_name") val serverName: String? = null,
    @SerialName("file_search_store") val fileSearchStore: String? = null,
    val annotations: Array<InteractionAnnotation>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InteractionContent

        if (type != other.type) return false
        if (text != other.text) return false
        if (data != other.data) return false
        if (uri != other.uri) return false
        if (mimeType != other.mimeType) return false
        if (processing != other.processing) return false
        if (resolution != other.resolution) return false
        if (signature != other.signature) return false
        if (summary != other.summary) return false
        if (name != other.name) return false
        if (arguments != other.arguments) return false
        if (isError != other.isError) return false
        if (result != other.result) return false
        if (callId != other.callId) return false
        if (language != other.language) return false
        if (code != other.code) return false
        if (urls != null) {
            if (other.urls == null) return false
            if (!urls.contentEquals(other.urls)) return false
        } else if (other.urls != null) {
            return false
        }
        if (status != other.status) return false
        if (title != other.title) return false
        if (renderedContent != other.renderedContent) return false
        if (serverName != other.serverName) return false
        if (fileSearchStore != other.fileSearchStore) return false
        if (annotations != null) {
            if (other.annotations == null) return false
            if (!annotations.contentEquals(other.annotations)) return false
        } else if (other.annotations != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result1 = type?.hashCode() ?: 0
        result1 = 31 * result1 + (text?.hashCode() ?: 0)
        result1 = 31 * result1 + (data?.hashCode() ?: 0)
        result1 = 31 * result1 + (uri?.hashCode() ?: 0)
        result1 = 31 * result1 + (mimeType?.hashCode() ?: 0)
        result1 = 31 * result1 + (processing?.hashCode() ?: 0)
        result1 = 31 * result1 + (resolution?.hashCode() ?: 0)
        result1 = 31 * result1 + (signature?.hashCode() ?: 0)
        result1 = 31 * result1 + (summary?.hashCode() ?: 0)
        result1 = 31 * result1 + (name?.hashCode() ?: 0)
        result1 = 31 * result1 + (arguments?.hashCode() ?: 0)
        result1 = 31 * result1 + (isError?.hashCode() ?: 0)
        result1 = 31 * result1 + (result?.hashCode() ?: 0)
        result1 = 31 * result1 + (callId?.hashCode() ?: 0)
        result1 = 31 * result1 + (language?.hashCode() ?: 0)
        result1 = 31 * result1 + (code?.hashCode() ?: 0)
        result1 = 31 * result1 + (urls?.contentHashCode() ?: 0)
        result1 = 31 * result1 + (status?.hashCode() ?: 0)
        result1 = 31 * result1 + (title?.hashCode() ?: 0)
        result1 = 31 * result1 + (renderedContent?.hashCode() ?: 0)
        result1 = 31 * result1 + (serverName?.hashCode() ?: 0)
        result1 = 31 * result1 + (fileSearchStore?.hashCode() ?: 0)
        result1 = 31 * result1 + (annotations?.contentHashCode() ?: 0)
        return result1
    }
}

/**
 * Optional summary wrapper for interaction thought content.
 *
 * @property content Summary content payload.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionThoughtSummary(
    val content: InteractionContent? = null,
)

/**
 * Annotation range and source metadata for interaction content.
 *
 * @property startIndex Optional inclusive start index in the associated content.
 * @property endIndex Optional exclusive end index in the associated content.
 * @property source Optional source identifier.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionAnnotation(
    @SerialName("start_index") val startIndex: Int? = null,
    @SerialName("end_index") val endIndex: Int? = null,
    val source: String? = null,
)
