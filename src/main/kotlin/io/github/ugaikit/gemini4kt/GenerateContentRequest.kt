package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class GenerateContentRequest(
    val contents: List<io.github.ugaikit.gemini4kt.Content>,
    val safetySettings: List<SafetySetting> = emptyList(),
    val generationConfig: GenerationConfig? = null,
)
