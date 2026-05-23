package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * The output from a server-side ToolCall execution.
 *
 * @property id The identifier of the tool call this response is for.
 * @property toolType The type of tool that was called, matching the toolType in the corresponding ToolCall.
 * @property response The tool response.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ToolResponse(
    val id: String? = null,
    val toolType: ToolType? = null,
    val response: Map<String, JsonElement>? = null,
)
