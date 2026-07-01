package io.github.ugaikit.gemini4kt

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents a request to generate content, specifying the inputs, tools, and
 * configurations required for content generation.
 *
 * @property contents A list of [Content] objects that serve as the input for the
 * content generation process. These can include text, data, or any form of content
 * that needs processing or augmentation.
 * @property tools An optional list of [Tool] objects to be used in the content
 * generation process. Each tool can apply specific transformations or analyses
 * to the input contents. Defaults to an empty list if no tools are specified.
 * @property toolConfig An optional [ToolConfig] that provides global configuration
 * settings for the tools involved in the generation process. It is nullable,
 * allowing for cases where no specific configuration is needed.
 * @property safetySettings An optional list of [SafetySetting] objects that define
 * safety-related configurations and constraints for the content generation. This
 * ensures that the generated content adheres to specified safety guidelines.
 * Defaults to an empty list if no safety settings are specified.
 * @property systemInstruction An optional [Content] object that provides additional
 * instructions or context to the system performing the content generation. This
 * can guide the generation process in a specific direction or ensure certain
 * considerations are made.
 * @property generationConfig An optional [GenerationConfig] that specifies detailed
 * configuration settings for the generation process, such as output format,
 * generation methods, and other technical parameters. It is nullable, allowing
 * for flexibility in cases where default configurations are sufficient.
 */
@OptIn(ExperimentalJsExport::class, ExperimentalSerializationApi::class)
@JsExport
@Serializable
data class GenerateContentRequest(
    val contents: Array<Content>,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val tools: Array<Tool> = emptyArray(),
    val toolConfig: ToolConfig? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val safetySettings: Array<SafetySetting> = emptyArray(),
    @SerialName("system_instruction")
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null,
    val cachedContent: String? = null,
)

/**
 * Represents the generate content request builder.
 */
class GenerateContentRequestBuilder {
    /**
     * Holds the contents.
     */
    private val contents: MutableList<Content> = mutableListOf()

    /**
     * Holds the tools.
     */
    private val tools: MutableList<Tool> = mutableListOf()

    /**
     * Holds the tool config.
     */
    private var toolConfig: ToolConfig? = null

    /**
     * Holds the safety settings.
     */
    private val safetySettings: MutableList<SafetySetting> = mutableListOf()

    /**
     * Holds the system instruction.
     */
    private var systemInstruction: Content? = null

    /**
     * Holds the generation config.
     */
    private var generationConfig: GenerationConfig? = null

    /**
     * Holds the cached content.
     */
    var cachedContent: String? = null

    /**
     * Handles content.
     *
     * @param init The init.
     */
    fun content(init: ContentBuilder.() -> Unit) {
        contents.add(ContentBuilder().apply(init).build())
    }

    /**
     * Handles tool.
     *
     * @param init The init.
     */
    fun tool(init: ToolBuilder.() -> Unit) {
        tools.add(ToolBuilder().apply(init).build())
    }

    /**
     * Handles tool config.
     *
     * @param init The init.
     */
    fun toolConfig(init: ToolConfigBuilder.() -> Unit) {
        toolConfig = ToolConfigBuilder().apply(init).build()
    }

    /**
     * Handles safety setting.
     *
     * @param init The init.
     */
    fun safetySetting(init: SafetySettingBuilder.() -> Unit) {
        safetySettings.add(SafetySettingBuilder().apply(init).build())
    }

    /**
     * Handles system instruction.
     *
     * @param init The init.
     */
    fun systemInstruction(init: ContentBuilder.() -> Unit) {
        systemInstruction = ContentBuilder().apply(init).build()
    }

    /**
     * Handles generation config.
     *
     * @param init The init.
     */
    fun generationConfig(init: GenerationConfigBuilder.() -> Unit) {
        generationConfig = GenerationConfigBuilder().apply(init).build()
    }

    /**
     * Handles build.
     */
    fun build(): GenerateContentRequest {
        require(contents.isNotEmpty()) { "At least one content item is required." }
        return GenerateContentRequest(
            contents = contents.toTypedArray(),
            tools = tools.toTypedArray(),
            toolConfig = toolConfig,
            safetySettings = safetySettings.toTypedArray(),
            systemInstruction = systemInstruction,
            generationConfig = generationConfig,
            cachedContent = cachedContent,
        )
    }
}

/**
 * Handles generate content request.
 *
 * @param init The init.
 */
fun generateContentRequest(init: GenerateContentRequestBuilder.() -> Unit): GenerateContentRequest =
    GenerateContentRequestBuilder()
        .apply(init)
        .build()
