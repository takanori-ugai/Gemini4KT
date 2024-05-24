package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a part of content, which can be one of several types such as text, inline data, function call, function response, or file data.
 *
 * @property text Optional plain text content of the part. It is nullable to accommodate parts that contain non-textual data.
 * @property inlineData Optional [InlineData] associated with this part, providing additional data inline.
 * It is nullable to accommodate parts without inline data.
 * @property functionCall Optional [FunctionCall] representing a call to a function within this part.
 * It is nullable to accommodate parts that are not function calls.
 * @property functionResponse Optional [FunctionResponse] representing the response from a function
 * call within this part.
 * It is nullable to accommodate parts that do not contain function responses.
 * @property fileData Optional [FileData] representing file data associated with this part.
 * It is nullable to accommodate parts that do not include file data.
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
