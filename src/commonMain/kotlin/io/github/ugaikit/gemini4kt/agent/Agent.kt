package io.github.ugaikit.gemini4kt.agent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Agent(
    val id: String,
    @SerialName("base_agent") val baseAgent: String? = null,
    @SerialName("system_instruction") val systemInstruction: String? = null,
    @SerialName("base_environment") val baseEnvironment: AgentEnvironment? = null,
    val created: String? = null,
    val updated: String? = null,
) {
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

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (baseAgent?.hashCode() ?: 0)
        result = 31 * result + (systemInstruction?.hashCode() ?: 0)
        result = 31 * result + (baseEnvironment?.hashCode() ?: 0)
        result = 31 * result + (created?.hashCode() ?: 0)
        result = 31 * result + (updated?.hashCode() ?: 0)
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
    @SerialName("base_environment") val baseEnvironment: AgentEnvironment? = null,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class AgentEnvironment(
    val type: String,
    val sources: Array<AgentSource>? = null,
) {
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

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (sources?.contentHashCode() ?: 0)
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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ListAgentsResponse

        if (!agents.contentEquals(other.agents)) return false
        if (nextPageToken != other.nextPageToken) return false

        return true
    }

    override fun hashCode(): Int {
        var result = agents.contentHashCode()
        result = 31 * result + (nextPageToken?.hashCode() ?: 0)
        return result
    }
}
