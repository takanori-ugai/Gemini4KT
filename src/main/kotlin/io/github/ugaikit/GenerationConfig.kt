package io.github.ugaikit

import kotlinx.serialization.Serializable

@Serializable
data class GenerationConfig(
    val stopSequences: List<String>,
    val temperature: Double,
    val maxOutputTokens: Int,
    val topP: Double,
    val topK: Int,
)
