package io.github.ugaikit

import kotlinx.serialization.Serializable

@Serializable
data class GenerateContentRequest(
    val contents: List<io.github.ugaikit.Content>,
    val safetySettings: List<SafetySetting> = emptyList(),
    val generationConfig: GenerationConfig? = null,
)
