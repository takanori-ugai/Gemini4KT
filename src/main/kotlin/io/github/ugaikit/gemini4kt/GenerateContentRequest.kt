package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val tools: List<Tool> = emptyList(),
    val toolConfig: ToolConfig? = null,
    val safetySettings: List<SafetySetting> = emptyList(),
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null,
)
