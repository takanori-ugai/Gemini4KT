package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val tools: List<Tool> = emptyList(),
    val toolConfig: ToolConfig? = null,
    val safetySettings: List<SafetySetting> = emptyList(),
    @SerialName("system_instruction")
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null,
    val cachedContent: String? = null,
)

class GenerateContentRequestBuilder {
    private val contents: MutableList<Content> = mutableListOf()
    private val tools: MutableList<Tool> = mutableListOf()
    private var toolConfig: ToolConfig? = null
    private val safetySettings: MutableList<SafetySetting> = mutableListOf()
    private var systemInstruction: Content? = null
    private var generationConfig: GenerationConfig? = null
    var cachedContent: String? = null

    fun content(init: ContentBuilder.() -> Unit) {
        contents.add(ContentBuilder().apply(init).build())
    }

    fun tool(init: ToolBuilder.() -> Unit) {
        tools.add(ToolBuilder().apply(init).build())
    }

    fun toolConfig(init: ToolConfigBuilder.() -> Unit) {
        toolConfig = ToolConfigBuilder().apply(init).build()
    }

    fun safetySetting(init: SafetySettingBuilder.() -> Unit) {
        safetySettings.add(SafetySettingBuilder().apply(init).build())
    }

    fun systemInstruction(init: ContentBuilder.() -> Unit) {
        systemInstruction = ContentBuilder().apply(init).build()
    }

    fun generationConfig(init: GenerationConfigBuilder.() -> Unit) {
        generationConfig = GenerationConfigBuilder().apply(init).build()
    }

    fun build() =
        GenerateContentRequest(
            contents = contents,
            tools = tools,
            toolConfig = toolConfig,
            safetySettings = safetySettings,
            systemInstruction = systemInstruction,
            generationConfig = generationConfig,
            cachedContent = cachedContent,
        )
}

fun generateContentRequest(init: GenerateContentRequestBuilder.() -> Unit): GenerateContentRequest = GenerateContentRequestBuilder().apply(init).build()
