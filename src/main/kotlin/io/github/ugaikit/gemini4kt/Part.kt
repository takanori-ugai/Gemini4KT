package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a component of a message or document, encapsulating different
 * types of content that can be included within a part.
 *
 * @property text The plain text content of the part, if available. Null if
 * this part does not contain text.
 * @property inlineData Data embedded directly within this part, if present.
 * Null if no inline data is included.
 * @property functionCall A call to a function represented by this part, if
 * applicable. Null if this part does not invoke a function.
 * @property functionResponse The response from a function call, if this part
 * represents such a response. Null if there is no function response.
 * @property fileData Information about a file associated with this part, if
 * any. Null if this part does not include file data.
 */
@Serializable
data class Part(
    val text: String? = null,
    @SerialName("inline_data")
    val inlineData: InlineData? = null,
    val functionCall: FunctionCall? = null,
    val functionResponse: FunctionResponse? = null,
    val fileData: FileData? = null,
)
