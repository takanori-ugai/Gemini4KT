package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a part of content, which can be one of several types such as text, inline data,
 * function call, function response, or file data.
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
 * @property executableCode Information about executable code associated with this part, if any.
 * @property codeExecutionResult Information about code execution result associated with this part, if any.
 * @property thoughtSignature The signature/hash of the thought.
 * @property thought True if this part represents a model's internal reasoning or thought processes.
 * @property partMetadata Key-value metadata dictionary for the part.
 * @property mediaResolution Media resolution configurations.
 * @property toolCall Predicted server-side tool call details.
 * @property toolResponse Execution response of a predicted server-side tool call.
 * @property videoMetadata Video start/end offset config.
 */
@OptIn(ExperimentalJsExport::class, kotlinx.serialization.ExperimentalSerializationApi::class)
@JsExport
@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null,
    val functionCall: FunctionCall? = null,
    val functionResponse: FunctionResponse? = null,
    val fileData: FileData? = null,
    val executableCode: ExecutableCode? = null,
    val codeExecutionResult: CodeExecutionResult? = null,
    @kotlinx.serialization.json.JsonNames("thought_signature")
    val thoughtSignature: String? = null,
    val thought: Boolean? = null,
    val partMetadata: Map<String, kotlinx.serialization.json.JsonElement>? = null,
    val mediaResolution: MediaResolution? = null,
    val toolCall: ToolCall? = null,
    val toolResponse: ToolResponse? = null,
    val videoMetadata: VideoMetadata? = null,
)

/**
 * Handles part.
 *
 * @param init The init.
 */
fun part(init: PartBuilder.() -> Unit): Part {
    /**
     * Holds the builder.
     */
    val builder = PartBuilder()
    builder.init()
    return builder.build()
}

/**
 * Represents the part builder.
 */
class PartBuilder {
    /**
     * Holds the text.
     */
    private var text: String? = null

    /**
     * Holds the inline data.
     */
    private var inlineData: InlineData? = null

    /**
     * Holds the function call.
     */
    private var functionCall: FunctionCall? = null

    /**
     * Holds the function response.
     */
    private var functionResponse: FunctionResponse? = null

    /**
     * Holds the file data.
     */
    private var fileData: FileData? = null

    /**
     * Holds the executable code.
     */
    private var executableCode: ExecutableCode? = null

    /**
     * Holds the code execution result.
     */
    private var codeExecutionResult: CodeExecutionResult? = null

    /**
     * Holds the thought signature.
     */
    private var thoughtSignature: String? = null

    /**
     * Holds the thought flag.
     */
    private var thought: Boolean? = null

    /**
     * Holds the part metadata.
     */
    private var partMetadata: Map<String, kotlinx.serialization.json.JsonElement>? = null

    /**
     * Holds the media resolution.
     */
    private var mediaResolution: MediaResolution? = null

    /**
     * Holds the tool call.
     */
    private var toolCall: ToolCall? = null

    /**
     * Holds the tool response.
     */
    private var toolResponse: ToolResponse? = null

    /**
     * Holds the video metadata.
     */
    private var videoMetadata: VideoMetadata? = null

    /**
     * Handles text.
     *
     * @param init The init.
     */
    fun text(init: () -> String?) = apply { text = init() }

    /**
     * Handles inline data.
     *
     * @param init The init.
     */
    fun inlineData(init: InlineDataBuilder.() -> Unit) =
        apply {
            inlineData = InlineDataBuilder().apply(init).build()
        }

    /**
     * Handles function call.
     *
     * @param init The init.
     */
    fun functionCall(init: FunctionCallBuilder.() -> Unit) =
        apply {
            functionCall = FunctionCallBuilder().apply(init).build()
        }

    /**
     * Handles function response.
     *
     * @param init The init.
     */
    fun functionResponse(init: () -> FunctionResponse?) = apply { functionResponse = init() }

    /**
     * Handles file data.
     *
     * @param init The init.
     */
    fun fileData(init: () -> FileData?) = apply { fileData = init() }

    /**
     * Handles executable code.
     *
     * @param init The init.
     */
    fun executableCode(init: ExecutableCodeBuilder.() -> Unit) =
        apply {
            executableCode = ExecutableCodeBuilder().apply(init).build()
        }

    /**
     * Handles code execution result.
     *
     * @param init The init.
     */
    fun codeExecutionResult(init: CodeExecutionResultBuilder.() -> Unit) =
        apply {
            codeExecutionResult = CodeExecutionResultBuilder().apply(init).build()
        }

    /**
     * Handles thought signature.
     *
     * @param init The init.
     */
    fun thoughtSignature(init: () -> String?) = apply { thoughtSignature = init() }

    /**
     * Handles thought flag.
     *
     * @param init The init.
     */
    fun thought(init: () -> Boolean?) = apply { thought = init() }

    /**
     * Handles part metadata.
     *
     * @param init The init.
     */
    fun partMetadata(init: () -> Map<String, kotlinx.serialization.json.JsonElement>?) = apply { partMetadata = init() }

    /**
     * Handles media resolution.
     *
     * @param init The init.
     */
    fun mediaResolution(init: () -> MediaResolution?) = apply { mediaResolution = init() }

    /**
     * Handles tool call.
     *
     * @param init The init.
     */
    fun toolCall(init: () -> ToolCall?) = apply { toolCall = init() }

    /**
     * Handles tool response.
     *
     * @param init The init.
     */
    fun toolResponse(init: () -> ToolResponse?) = apply { toolResponse = init() }

    /**
     * Handles video metadata.
     *
     * @param init The init.
     */
    fun videoMetadata(init: () -> VideoMetadata?) = apply { videoMetadata = init() }

    /**
     * Handles build.
     */
    fun build() =
        Part(
            text = text,
            inlineData = inlineData,
            functionCall = functionCall,
            functionResponse = functionResponse,
            fileData = fileData,
            executableCode = executableCode,
            codeExecutionResult = codeExecutionResult,
            thoughtSignature = thoughtSignature,
            thought = thought,
            partMetadata = partMetadata,
            mediaResolution = mediaResolution,
            toolCall = toolCall,
            toolResponse = toolResponse,
            videoMetadata = videoMetadata,
        ).also {
            val activePayloadCount =
                listOfNotNull(
                    text,
                    inlineData,
                    functionCall,
                    functionResponse,
                    fileData,
                    executableCode,
                    codeExecutionResult,
                    toolCall,
                    toolResponse,
                ).size
            require(activePayloadCount <= 1) {
                "Part builders support only one primary payload field at a time."
            }
        }
}
