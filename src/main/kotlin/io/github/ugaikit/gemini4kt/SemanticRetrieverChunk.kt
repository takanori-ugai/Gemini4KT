package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class SemanticRetrieverChunk(
    val source: String,
    val chunk: String,
)
