package io.github.ugaikit.gemini4kt.interaction

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.SpeechConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Persisted interaction object returned by the Interaction API.
 *
 * @property id Interaction identifier.
 * @property created Optional creation timestamp.
 * @property updated Optional last-updated timestamp.
 * @property model Optional model name used by the interaction.
 * @property agent Optional agent name or ID.
 * @property role Optional role associated with the interaction output.
 * @property status Current interaction status.
 * @property outputs Optional output content items.
 * @property usage Optional token usage metrics.
 * @property systemInstruction Optional system instruction text.
 * @property tools Optional tool configuration associated with the interaction.
 * @property background Optional background execution flag.
 * @property responseModalities Optional response modalities requested.
 * @property responseFormat Optional structured response format descriptor.
 * @property responseMimeType Optional MIME type for response payloads.
 * @property previousInteractionId Optional previous interaction ID for continuity.
 * @property input Optional raw input payload.
 * @property generationConfig Optional generation configuration.
 * @property agentConfig Optional agent runtime configuration.
 * @property environmentId Optional execution environment ID.
 * @property outputTextRaw Optional plain-text aggregate output as returned by the API.
 * @property outputText Derived plain-text aggregate output, falling back to the last model step.
 * @property steps Optional step-level execution details.
 */
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
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
    @SerialName("response_mime_type") val responseMimeType: String? = null,
    @SerialName("previous_interaction_id")
    val previousInteractionId: String? = null,
    val input: InteractionInput? = null,
    @SerialName("generation_config") val generationConfig: InteractionGenerationConfig? = null,
    @SerialName("agent_config") val agentConfig: InteractionAgentConfig? = null,
    @SerialName("environment_id") val environmentId: String? = null,
    @SerialName("cached_content") val cachedContent: String? = null,
    val environment: InteractionEnvironment? = null,
    @SerialName("service_tier") val serviceTier: JsonElement? = null,
    @SerialName("webhook_config") val webhookConfig: JsonElement? = null,
    @SerialName("output_text") val outputTextRaw: String? = null,
    @SerialName("output_image") val outputImage: InteractionImageContent? = null,
    @SerialName("output_audio") val outputAudio: InteractionAudioContent? = null,
    @SerialName("output_video") val outputVideo: InteractionVideoContent? = null,
    val steps: Array<InteractionStep>? = null,
) {
    val outputText: String?
        get() = outputTextRaw ?: steps?.resolvedOutputText()

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
        if (environmentId != other.environmentId) return false
        if (cachedContent != other.cachedContent) return false
        if (environment != other.environment) return false
        if (serviceTier != other.serviceTier) return false
        if (webhookConfig != other.webhookConfig) return false
        if (outputTextRaw != other.outputTextRaw) return false
        if (outputImage != other.outputImage) return false
        if (outputAudio != other.outputAudio) return false
        if (outputVideo != other.outputVideo) return false
        if (steps != null) {
            if (other.steps == null) return false
            if (!steps.contentEquals(other.steps)) return false
        } else if (other.steps != null) {
            return false
        }

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
        result = 31 * result + (environmentId?.hashCode() ?: 0)
        result = 31 * result + (cachedContent?.hashCode() ?: 0)
        result = 31 * result + (environment?.hashCode() ?: 0)
        result = 31 * result + (serviceTier?.hashCode() ?: 0)
        result = 31 * result + (webhookConfig?.hashCode() ?: 0)
        result = 31 * result + (outputTextRaw?.hashCode() ?: 0)
        result = 31 * result + (outputImage?.hashCode() ?: 0)
        result = 31 * result + (outputAudio?.hashCode() ?: 0)
        result = 31 * result + (outputVideo?.hashCode() ?: 0)
        result = 31 * result + (steps?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Request payload for creating a new interaction.
 *
 * @property model Optional model name.
 * @property agent Optional agent name or ID.
 * @property input Optional input payload.
 * @property systemInstruction Optional system instruction text.
 * @property tools Optional tool configuration.
 * @property responseFormat Optional structured response format descriptor.
 * @property responseMimeType Optional MIME type for response payloads.
 * @property stream Optional streaming response flag.
 * @property store Optional persistence flag.
 * @property background Optional background execution flag.
 * @property generationConfig Optional generation configuration.
 * @property agentConfig Optional agent runtime configuration.
 * @property responseModalities Optional response modalities requested.
 * @property previousInteractionId Optional prior interaction ID for continuity.
 * @property environment Optional environment payload.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CreateInteractionRequest(
    val model: String? = null,
    val agent: String? = null,
    val input: InteractionInput,
    @SerialName("system_instruction") val systemInstruction: String? = null,
    val tools: Array<InteractionTool>? = null,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
    @SerialName("response_mime_type") val responseMimeType: String? = null,
    val stream: Boolean? = null,
    val store: Boolean? = null,
    val background: Boolean? = null,
    @SerialName("generation_config") val generationConfig: InteractionGenerationConfig? = null,
    @SerialName("agent_config") val agentConfig: InteractionAgentConfig? = null,
    @SerialName("response_modalities") val responseModalities: Array<InteractionResponseModality>? = null,
    @SerialName("previous_interaction_id") val previousInteractionId: String? = null,
    val environment: InteractionEnvironment? = null,
) {
    @JsName("fromTextInput")
    constructor(
        model: String? = null,
        agent: String? = null,
        input: String,
        systemInstruction: String? = null,
        tools: Array<InteractionTool>? = null,
        responseFormat: ResponseFormat? = null,
        responseMimeType: String? = null,
        stream: Boolean? = null,
        store: Boolean? = null,
        background: Boolean? = null,
        generationConfig: InteractionGenerationConfig? = null,
        agentConfig: InteractionAgentConfig? = null,
        responseModalities: Array<InteractionResponseModality>? = null,
        previousInteractionId: String? = null,
        environment: InteractionEnvironment? = null,
    ) : this(
        model = model,
        agent = agent,
        input = InteractionInput.Text(input),
        systemInstruction = systemInstruction,
        tools = tools,
        responseFormat = responseFormat,
        responseMimeType = responseMimeType,
        stream = stream,
        store = store,
        background = background,
        generationConfig = generationConfig,
        agentConfig = agentConfig,
        responseModalities = responseModalities,
        previousInteractionId = previousInteractionId,
        environment = environment,
    )

    @JsName("fromContentInput")
    constructor(
        model: String? = null,
        agent: String? = null,
        input: Content,
        systemInstruction: String? = null,
        tools: Array<InteractionTool>? = null,
        responseFormat: ResponseFormat? = null,
        responseMimeType: String? = null,
        stream: Boolean? = null,
        store: Boolean? = null,
        background: Boolean? = null,
        generationConfig: InteractionGenerationConfig? = null,
        agentConfig: InteractionAgentConfig? = null,
        responseModalities: Array<InteractionResponseModality>? = null,
        previousInteractionId: String? = null,
        environment: InteractionEnvironment? = null,
    ) : this(
        model = model,
        agent = agent,
        input = InteractionInput.SingleContent(input),
        systemInstruction = systemInstruction,
        tools = tools,
        responseFormat = responseFormat,
        responseMimeType = responseMimeType,
        stream = stream,
        store = store,
        background = background,
        generationConfig = generationConfig,
        agentConfig = agentConfig,
        responseModalities = responseModalities,
        previousInteractionId = previousInteractionId,
        environment = environment,
    )

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
        if (environment != other.environment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model?.hashCode() ?: 0
        result = 31 * result + (agent?.hashCode() ?: 0)
        result = 31 * result + input.hashCode()
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
        result = 31 * result + (environment?.hashCode() ?: 0)
        return result
    }
}

/**
 * Token usage metrics for an interaction.
 *
 * @property totalInputTokens Total input tokens.
 * @property inputTokensByModality Input token totals grouped by modality.
 * @property totalCachedTokens Total cached tokens.
 * @property cachedTokensByModality Cached token totals grouped by modality.
 * @property totalOutputTokens Total output tokens.
 * @property outputTokensByModality Output token totals grouped by modality.
 * @property totalToolUseTokens Total tool-use tokens.
 * @property toolUseTokensByModality Tool-use token totals grouped by modality.
 * @property totalReasoningTokens Total reasoning tokens.
 * @property totalTokens Total tokens across all categories.
 */
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
    @SerialName("tool_use_tokens_by_modality")
    val toolUseTokensByModality: Array<InteractionModalityTokenCount>? = null,
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

/**
 * Token count for a specific output/input modality.
 *
 * @property modality Modality associated with the token count.
 * @property tokens Token count.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionModalityTokenCount(
    val modality: InteractionResponseModality,
    val tokens: Int,
)

/**
 * Generation settings for interaction responses.
 *
 * @property temperature Sampling temperature.
 * @property topP Nucleus sampling threshold.
 * @property seed Optional random seed.
 * @property stopSequences Optional stop sequences.
 * @property toolChoice Optional tool selection policy.
 * @property thinkingLevel Optional reasoning depth preset.
 * @property thinkingSummaries Optional reasoning summary mode.
 * @property maxOutputTokens Optional output token limit.
 * @property speechConfig Optional speech configuration.
 */
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
    @SerialName("speech_config") val speechConfig: SpeechConfig? = null,
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
        if (speechConfig != other.speechConfig) return false

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
        result = 31 * result + (speechConfig?.hashCode() ?: 0)
        return result
    }
}

/**
 * Agent execution configuration for an interaction.
 *
 * @property type Agent type identifier.
 * @property thinkingSummaries Optional reasoning summary mode for the agent.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionAgentConfig(
    val type: String,
    @SerialName("thinking_summaries") val thinkingSummaries: ThinkingSummaries? = null,
)

/**
 * Turn payload representing a role-tagged content item.
 *
 * @property role Role name for the turn.
 * @property content Turn content payload.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionTurn(
    val role: String,
    val content: JsonElement,
)

/**
 * Step payload used by the Interactions API multi-turn input shape.
 *
 * @property type Step type identifier.
 * @property id Identifier for a processing call step.
 * @property callId Identifier linking a processing result to its call.
 * @property signature Optional step signature.
 * @property content Step content payload.
 * @property summary Thought summary content.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class InteractionStep(
    val type: String,
    val content: Array<InteractionContent>? = null,
    val id: String? = null,
    @SerialName("call_id") val callId: String? = null,
    val signature: String? = null,
    val summary: Array<InteractionContent>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InteractionStep

        if (type != other.type) return false
        if (id != other.id) return false
        if (callId != other.callId) return false
        if (signature != other.signature) return false
        if (content != null) {
            if (other.content == null) return false
            if (!content.contentEquals(other.content)) return false
        } else if (other.content != null) {
            return false
        }
        if (summary != null) {
            if (other.summary == null) return false
            if (!summary.contentEquals(other.summary)) return false
        } else if (other.summary != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (callId?.hashCode() ?: 0)
        result = 31 * result + (signature?.hashCode() ?: 0)
        result = 31 * result + (content?.contentHashCode() ?: 0)
        result = 31 * result + (summary?.contentHashCode() ?: 0)
        return result
    }
}

internal fun Array<InteractionStep>.resolvedOutputText(): String? {
    val lastModelOutput = lastOrNull { it.type == "model_output" }
    val content = lastModelOutput?.content ?: return null

    val text = content.mapNotNull { it.text?.takeIf(String::isNotBlank) }
    return text.takeIf { it.isNotEmpty() }?.joinToString(separator = "\n")
}
