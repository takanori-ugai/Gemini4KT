package io.github.ugaikit.gemini4kt.interaction

import io.github.ugaikit.gemini4kt.SpeechConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Interaction(
    val id: String,
    val created: String? = null,
    val updated: String? = null,
    val model: String? = null,
    val agent: String? = null,
    val role: String? = null,
    val status: InteractionStatus,
    val outputs: Array<InteractionContent>? = null,
    val usage: InteractionUsage? = null,
    @SerialName("system_instruction") val systemInstruction: String? = null,
    val tools: Array<InteractionTool>? = null,
    val background: Boolean? = null,
    @SerialName("response_modalities") val responseModalities: Array<InteractionResponseModality>? = null,
    @SerialName("response_format") val responseFormat: JsonElement? = null,
    @SerialName("response_mime_type") val responseMimeType: String? = null,
    @SerialName("previous_interaction_id")
    val previousInteractionId: String? = null,
    val input: JsonElement? = null,
    @SerialName("generation_config") val generationConfig: InteractionGenerationConfig? = null,
    @SerialName("agent_config") val agentConfig: InteractionAgentConfig? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Interaction

        if (id != other.id) return false
        if (created != other.created) return false
        if (updated != other.updated) return false
        if (model != other.model) return false
        if (agent != other.agent) return false
        if (role != other.role) return false
        if (status != other.status) return false
        if (outputs != null) {
            if (other.outputs == null) return false
            if (!outputs.contentEquals(other.outputs)) return false
        } else if (other.outputs != null) {
            return false
        }
        if (usage != other.usage) return false
        if (systemInstruction != other.systemInstruction) return false
        if (tools != null) {
            if (other.tools == null) return false
            if (!tools.contentEquals(other.tools)) return false
        } else if (other.tools != null) {
            return false
        }
        if (background != other.background) return false
        if (responseModalities != null) {
            if (other.responseModalities == null) return false
            if (!responseModalities.contentEquals(other.responseModalities)) return false
        } else if (other.responseModalities != null) {
            return false
        }
        if (responseFormat != other.responseFormat) return false
        if (responseMimeType != other.responseMimeType) return false
        if (previousInteractionId != other.previousInteractionId) return false
        if (input != other.input) return false
        if (generationConfig != other.generationConfig) return false
        if (agentConfig != other.agentConfig) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (created?.hashCode() ?: 0)
        result = 31 * result + (updated?.hashCode() ?: 0)
        result = 31 * result + (model?.hashCode() ?: 0)
        result = 31 * result + (agent?.hashCode() ?: 0)
        result = 31 * result + (role?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + (outputs?.contentHashCode() ?: 0)
        result = 31 * result + (usage?.hashCode() ?: 0)
        result = 31 * result + (systemInstruction?.hashCode() ?: 0)
        result = 31 * result + (tools?.contentHashCode() ?: 0)
        result = 31 * result + (background?.hashCode() ?: 0)
        result = 31 * result + (responseModalities?.contentHashCode() ?: 0)
        result = 31 * result + (responseFormat?.hashCode() ?: 0)
        result = 31 * result + (responseMimeType?.hashCode() ?: 0)
        result = 31 * result + (previousInteractionId?.hashCode() ?: 0)
        result = 31 * result + (input?.hashCode() ?: 0)
        result = 31 * result + (generationConfig?.hashCode() ?: 0)
        result = 31 * result + (agentConfig?.hashCode() ?: 0)
        return result
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CreateInteractionRequest(
    val model: String? = null,
    val agent: String? = null,
    val input: JsonElement? = null,
    @SerialName("system_instruction") val systemInstruction: String? = null,
    val tools: Array<InteractionTool>? = null,
    @SerialName("response_format") val responseFormat: JsonElement? = null,
    @SerialName("response_mime_type") val responseMimeType: String? = null,
    val stream: Boolean? = null,
    val store: Boolean? = null,
    val background: Boolean? = null,
    @SerialName("generation_config") val generationConfig: InteractionGenerationConfig? = null,
    @SerialName("agent_config") val agentConfig: InteractionAgentConfig? = null,
    @SerialName("response_modalities") val responseModalities: Array<InteractionResponseModality>? = null,
    @SerialName("previous_interaction_id") val previousInteractionId: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CreateInteractionRequest

        if (model != other.model) return false
        if (agent != other.agent) return false
        if (input != other.input) return false
        if (systemInstruction != other.systemInstruction) return false
        if (tools != null) {
            if (other.tools == null) return false
            if (!tools.contentEquals(other.tools)) return false
        } else if (other.tools != null) {
            return false
        }
        if (responseFormat != other.responseFormat) return false
        if (responseMimeType != other.responseMimeType) return false
        if (stream != other.stream) return false
        if (store != other.store) return false
        if (background != other.background) return false
        if (generationConfig != other.generationConfig) return false
        if (agentConfig != other.agentConfig) return false
        if (responseModalities != null) {
            if (other.responseModalities == null) return false
            if (!responseModalities.contentEquals(other.responseModalities)) return false
        } else if (other.responseModalities != null) {
            return false
        }
        if (previousInteractionId != other.previousInteractionId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model?.hashCode() ?: 0
        result = 31 * result + (agent?.hashCode() ?: 0)
        result = 31 * result + (input?.hashCode() ?: 0)
        result = 31 * result + (systemInstruction?.hashCode() ?: 0)
        result = 31 * result + (tools?.contentHashCode() ?: 0)
        result = 31 * result + (responseFormat?.hashCode() ?: 0)
        result = 31 * result + (responseMimeType?.hashCode() ?: 0)
        result = 31 * result + (stream?.hashCode() ?: 0)
        result = 31 * result + (store?.hashCode() ?: 0)
        result = 31 * result + (background?.hashCode() ?: 0)
        result = 31 * result + (generationConfig?.hashCode() ?: 0)
        result = 31 * result + (agentConfig?.hashCode() ?: 0)
        result = 31 * result + (responseModalities?.contentHashCode() ?: 0)
        result = 31 * result + (previousInteractionId?.hashCode() ?: 0)
        return result
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionUsage(
    @SerialName("total_input_tokens") val totalInputTokens: Int? = null,
    @SerialName("input_tokens_by_modality") val inputTokensByModality: Array<InteractionModalityTokenCount>? = null,
    @SerialName("total_cached_tokens") val totalCachedTokens: Int? = null,
    @SerialName("cached_tokens_by_modality") val cachedTokensByModality: Array<InteractionModalityTokenCount>? = null,
    @SerialName("total_output_tokens") val totalOutputTokens: Int? = null,
    @SerialName("output_tokens_by_modality") val outputTokensByModality: Array<InteractionModalityTokenCount>? = null,
    @SerialName("total_tool_use_tokens") val totalToolUseTokens: Int? = null,
    @SerialName("tool_use_tokens_by_modality") val toolUseTokensByModality: Array<InteractionModalityTokenCount>? = null,
    @SerialName("total_reasoning_tokens") val totalReasoningTokens: Int? = null,
    @SerialName("total_tokens") val totalTokens: Int? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InteractionUsage

        if (totalInputTokens != other.totalInputTokens) return false
        if (inputTokensByModality != null) {
            if (other.inputTokensByModality == null) return false
            if (!inputTokensByModality.contentEquals(other.inputTokensByModality)) return false
        } else if (other.inputTokensByModality != null) {
            return false
        }
        if (totalCachedTokens != other.totalCachedTokens) return false
        if (cachedTokensByModality != null) {
            if (other.cachedTokensByModality == null) return false
            if (!cachedTokensByModality.contentEquals(other.cachedTokensByModality)) return false
        } else if (other.cachedTokensByModality != null) {
            return false
        }
        if (totalOutputTokens != other.totalOutputTokens) return false
        if (outputTokensByModality != null) {
            if (other.outputTokensByModality == null) return false
            if (!outputTokensByModality.contentEquals(other.outputTokensByModality)) return false
        } else if (other.outputTokensByModality != null) {
            return false
        }
        if (totalToolUseTokens != other.totalToolUseTokens) return false
        if (toolUseTokensByModality != null) {
            if (other.toolUseTokensByModality == null) return false
            if (!toolUseTokensByModality.contentEquals(other.toolUseTokensByModality)) return false
        } else if (other.toolUseTokensByModality != null) {
            return false
        }
        if (totalReasoningTokens != other.totalReasoningTokens) return false
        if (totalTokens != other.totalTokens) return false

        return true
    }

    override fun hashCode(): Int {
        var result = totalInputTokens ?: 0
        result = 31 * result + (inputTokensByModality?.contentHashCode() ?: 0)
        result = 31 * result + (totalCachedTokens ?: 0)
        result = 31 * result + (cachedTokensByModality?.contentHashCode() ?: 0)
        result = 31 * result + (totalOutputTokens ?: 0)
        result = 31 * result + (outputTokensByModality?.contentHashCode() ?: 0)
        result = 31 * result + (totalToolUseTokens ?: 0)
        result = 31 * result + (toolUseTokensByModality?.contentHashCode() ?: 0)
        result = 31 * result + (totalReasoningTokens ?: 0)
        result = 31 * result + (totalTokens ?: 0)
        return result
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionModalityTokenCount(
    val modality: InteractionResponseModality,
    val tokens: Int,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionGenerationConfig(
    val temperature: Double? = null,
    @SerialName("top_p") val topP: Double? = null,
    val seed: Int? = null,
    @SerialName("stop_sequences") val stopSequences: Array<String>? = null,
    @SerialName("tool_choice") val toolChoice: JsonElement? = null,
    @SerialName("thinking_level") val thinkingLevel: ThinkingLevel? = null,
    @SerialName("thinking_summaries") val thinkingSummaries: ThinkingSummaries? = null,
    @SerialName("max_output_tokens") val maxOutputTokens: Int? = null,
    @SerialName("speech_config") val speechConfig: Array<SpeechConfig>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InteractionGenerationConfig

        if (temperature != other.temperature) return false
        if (topP != other.topP) return false
        if (seed != other.seed) return false
        if (stopSequences != null) {
            if (other.stopSequences == null) return false
            if (!stopSequences.contentEquals(other.stopSequences)) return false
        } else if (other.stopSequences != null) {
            return false
        }
        if (toolChoice != other.toolChoice) return false
        if (thinkingLevel != other.thinkingLevel) return false
        if (thinkingSummaries != other.thinkingSummaries) return false
        if (maxOutputTokens != other.maxOutputTokens) return false
        if (speechConfig != null) {
            if (other.speechConfig == null) return false
            if (!speechConfig.contentEquals(other.speechConfig)) return false
        } else if (other.speechConfig != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = temperature?.hashCode() ?: 0
        result = 31 * result + (topP?.hashCode() ?: 0)
        result = 31 * result + (seed ?: 0)
        result = 31 * result + (stopSequences?.contentHashCode() ?: 0)
        result = 31 * result + (toolChoice?.hashCode() ?: 0)
        result = 31 * result + (thinkingLevel?.hashCode() ?: 0)
        result = 31 * result + (thinkingSummaries?.hashCode() ?: 0)
        result = 31 * result + (maxOutputTokens ?: 0)
        result = 31 * result + (speechConfig?.contentHashCode() ?: 0)
        return result
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionAgentConfig(
    val type: String,
    @SerialName("thinking_summaries") val thinkingSummaries: ThinkingSummaries? = null,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionTurn(
    val role: String,
    val content: JsonElement,
)
