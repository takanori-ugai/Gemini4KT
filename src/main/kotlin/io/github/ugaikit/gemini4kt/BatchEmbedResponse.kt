package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class BatchEmbedResponse(val embeddings: List<io.github.ugaikit.gemini4kt.Values>)
