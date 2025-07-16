package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a part of content, which can be one of several types such as text, inline data, function call, function response, or file data.
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

fun part(init: PartBuilder.() -> Unit): Part {
    val builder = PartBuilder()
    builder.init()
    return builder.build()
}

class PartBuilder {
    private var text: String? = null
    private var inlineData: InlineData? = null
    private var functionCall: FunctionCall? = null
    private var functionResponse: FunctionResponse? = null
    private var fileData: FileData? = null

    fun text(init: () -> String?) = apply { text = init() }

    fun inlineData(init: InlineDataBuilder.() -> Unit) = apply { inlineData = InlineDataBuilder().apply(init).build() }

    fun functionCall(init: FunctionCallBuilder.() -> Unit) = apply { functionCall = FunctionCallBuilder().apply(init).build() }

    fun functionResponse(init: () -> FunctionResponse?) = apply { functionResponse = init() }

    fun fileData(init: () -> FileData?) = apply { fileData = init() }

    fun build() =
        Part(
            text = text,
            inlineData = inlineData,
            functionCall = functionCall,
            functionResponse = functionResponse,
            fileData = fileData,
        )
}
