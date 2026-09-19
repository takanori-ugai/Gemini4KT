package io.github.ugaikit.gemini4kt.agent

import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import io.github.ugaikit.gemini4kt.interaction.ThinkingSummaries
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsExport.Ignore

/**
 * Base environment used when creating or invoking a managed agent.
 *
 * The REST API accepts either an environment configuration object or an existing
 * environment ID string.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable(with = AgentBaseEnvironmentSerializer::class)
sealed interface AgentBaseEnvironment

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class AgentEnvironmentReference(
    val id: String,
) : AgentBaseEnvironment

/** Serializes an environment union as either an ID string or an environment object. */
object AgentBaseEnvironmentSerializer : KSerializer<AgentBaseEnvironment> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("io.github.ugaikit.gemini4kt.agent.AgentBaseEnvironment")

    /** Encodes the environment union in the JSON shape expected by the REST API. */
    override fun serialize(
        encoder: Encoder,
        value: AgentBaseEnvironment,
    ) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: throw SerializationException("AgentBaseEnvironment can only be serialized as JSON.")

        when (value) {
            is AgentEnvironmentReference -> jsonEncoder.encodeString(value.id)
            is AgentEnvironment -> jsonEncoder.encodeSerializableValue(AgentEnvironment.serializer(), value)
        }
    }

    /** Decodes either a string environment reference or an inline environment object. */
    override fun deserialize(decoder: Decoder): AgentBaseEnvironment {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("AgentBaseEnvironment can only be deserialized as JSON.")

        val element = jsonDecoder.decodeJsonElement()
        return when (element) {
            is JsonPrimitive ->
                if (element.isString) {
                    AgentEnvironmentReference(element.content)
                } else {
                    throw SerializationException("Agent environment references must be strings.")
                }
            is JsonObject -> jsonDecoder.json.decodeFromJsonElement(AgentEnvironment.serializer(), element)
            else -> throw SerializationException("Agent base environment must be a string or object.")
        }
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Agent(
    val id: String,
    @SerialName("base_agent") val baseAgent: String? = null,
    @SerialName("system_instruction") val systemInstruction: String? = null,
    @SerialName("base_environment") val baseEnvironment: AgentBaseEnvironment? = null,
    val created: String? = null,
    val updated: String? = null,
    val description: String? = null,
    @SerialName("agent_config") val agentConfig: AgentConfig? = null,
    val tools: Array<InteractionTool>? = null,
) {
    /** Compares all scalar fields and array contents of two agent responses. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Agent

        if (id != other.id) return false
        if (baseAgent != other.baseAgent) return false
        if (systemInstruction != other.systemInstruction) return false
        if (baseEnvironment != other.baseEnvironment) return false
        if (created != other.created) return false
        if (updated != other.updated) return false
        if (description != other.description) return false
        if (agentConfig != other.agentConfig) return false
        if (tools != null) {
            if (other.tools == null) return false
            if (!tools.contentEquals(other.tools)) return false
        } else if (other.tools != null) {
            return false
        }

        return true
    }

    /** Computes a hash using the same content-based array semantics as [equals]. */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (baseAgent?.hashCode() ?: 0)
        result = 31 * result + (systemInstruction?.hashCode() ?: 0)
        result = 31 * result + (baseEnvironment?.hashCode() ?: 0)
        result = 31 * result + (created?.hashCode() ?: 0)
        result = 31 * result + (updated?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (agentConfig?.hashCode() ?: 0)
        result = 31 * result + (tools?.contentHashCode() ?: 0)
        return result
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CreateAgentRequest(
    val id: String,
    @SerialName("base_agent") val baseAgent: String? = null,
    @SerialName("system_instruction") val systemInstruction: String? = null,
    @SerialName("base_environment") val baseEnvironment: AgentBaseEnvironment? = null,
    val description: String? = null,
    @SerialName("agent_config") val agentConfig: AgentConfig? = null,
    val tools: Array<InteractionTool>? = null,
)

/**
 * Configuration for the managed agent's underlying Antigravity model.
 *
 * @property type Agent type identifier, normally `antigravity`.
 * @property model Optional Gemini model selection.
 * @property maxTotalTokens Optional total input, output, and thinking token budget.
 * @property thinkingSummaries Optional reasoning summary mode.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class AgentConfig(
    val type: String,
    val model: String? = null,
    @SerialName("max_total_tokens") val maxTotalTokens: Int? = null,
    @SerialName("thinking_summaries") val thinkingSummaries: ThinkingSummaries? = null,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class AgentEnvironment(
    val type: String,
    val sources: Array<AgentSource>? = null,
    @Ignore
    val network: kotlinx.serialization.json.JsonElement? = null,
) : AgentBaseEnvironment {
    /** Compares environment metadata, sources, and the raw network configuration. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AgentEnvironment

        if (type != other.type) return false
        if (sources != null) {
            if (other.sources == null) return false
            if (!sources.contentEquals(other.sources)) return false
        } else if (other.sources != null) {
            return false
        }
        if (network != other.network) return false

        return true
    }

    /** Computes a hash using content-based source array semantics. */
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (sources?.contentHashCode() ?: 0)
        result = 31 * result + (network?.hashCode() ?: 0)
        return result
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class AgentSource(
    val type: String,
    val target: String? = null,
    val content: String? = null,
    val source: String? = null,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ListAgentsResponse(
    val agents: Array<Agent>,
    @SerialName("nextPageToken") val nextPageToken: String? = null,
) {
    /** Compares the response agents by content and the pagination token. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ListAgentsResponse

        if (!agents.contentEquals(other.agents)) return false
        if (nextPageToken != other.nextPageToken) return false

        return true
    }

    /** Computes a hash using content-based agent array semantics. */
    override fun hashCode(): Int {
        var result = agents.contentHashCode()
        result = 31 * result + (nextPageToken?.hashCode() ?: 0)
        return result
    }
}
