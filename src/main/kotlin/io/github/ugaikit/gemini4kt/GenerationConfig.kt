package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerationConfig(
    val stopSequences: List<String>,
    val temperature: Double,
    val maxOutputTokens: Int,
    val topP: Double,
    val topK: Int,
    @SerialName("response_mime_type")
    val responseMimeType: String? = null,
    // "application/json" only for Gemini 1.5 pro
)
