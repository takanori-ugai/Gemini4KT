package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * A predicted server-side ToolCall returned from the model.
 *
 * @property id Unique identifier of the tool call.
 * @property toolType The type of tool that was called.
 * @property args The tool call arguments.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ToolCall(
    val id: String? = null,
    val toolType: ToolType? = null,
    val args: Map<String, JsonElement>? = null,
)
