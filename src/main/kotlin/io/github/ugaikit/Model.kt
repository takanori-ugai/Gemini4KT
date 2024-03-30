package io.github.ugaikit

import kotlinx.serialization.Serializable

@Serializable
data class Model(
    val name: String,
    val version: String,
    val displayName: String,
    val description: String,
    val inputTokenLimit: Int,
    val outputTokenLimit: Int,
    val supportedGenerationMethods: List<String>,
    val temperature: Double? = null,
    val topP: Double? = null,
    val topK: Int? = null,
)
