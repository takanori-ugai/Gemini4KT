package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
@JsExport
data class UsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int,
    val promptTokensDetails: Array<ModalityTokenCount>? = null,
    val toolUsePromptTokenCount: Int? = null,
    val toolUsePromptTokensDetails: Array<ModalityTokenCount>? = null,
    val thoughtsTokenCount: Int? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UsageMetadata

        if (promptTokenCount != other.promptTokenCount) return false
        if (candidatesTokenCount != other.candidatesTokenCount) return false
        if (totalTokenCount != other.totalTokenCount) return false
        if (promptTokensDetails != null) {
            if (other.promptTokensDetails == null) return false
            if (!promptTokensDetails.contentEquals(other.promptTokensDetails)) return false
        } else if (other.promptTokensDetails != null) {
            return false
        }
        if (toolUsePromptTokenCount != other.toolUsePromptTokenCount) return false
        if (toolUsePromptTokensDetails != null) {
            if (other.toolUsePromptTokensDetails == null) return false
            if (!toolUsePromptTokensDetails.contentEquals(other.toolUsePromptTokensDetails)) return false
        } else if (other.toolUsePromptTokensDetails != null) {
            return false
        }
        if (thoughtsTokenCount != other.thoughtsTokenCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = promptTokenCount ?: 0
        result = 31 * result + (candidatesTokenCount ?: 0)
        result = 31 * result + totalTokenCount
        result = 31 * result + (promptTokensDetails?.contentHashCode() ?: 0)
        result = 31 * result + (toolUsePromptTokenCount ?: 0)
        result = 31 * result + (toolUsePromptTokensDetails?.contentHashCode() ?: 0)
        result = 31 * result + (thoughtsTokenCount ?: 0)
        return result
    }
}

@Serializable
@JsExport
data class ModalityTokenCount(
    val modality: Modality,
    val tokenCount: Int,
)
