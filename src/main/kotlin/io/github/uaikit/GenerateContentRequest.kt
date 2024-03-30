package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class GenerateContentRequest(
    val contents: List<io.github.uaikit.Content>,
    val safetySettings: List<SafetySetting> = emptyList(),
    val generationConfig: GenerationConfig? = null,
)
