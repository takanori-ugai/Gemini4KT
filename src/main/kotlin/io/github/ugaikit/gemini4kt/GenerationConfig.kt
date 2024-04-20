package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class GenerationConfig(
    val stopSequences: List<String>,
    val temperature: Double,
    val maxOutputTokens: Int,
    val topP: Double,
    val topK: Int,
    val response_mime_type: String? = null,
    // "application/json" only for Gemini 1.5 pro
)
