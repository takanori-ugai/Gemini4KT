package io.github.ugaikit

import kotlinx.serialization.Serializable

@Serializable
data class BatchEmbedResponse(val embeddings: List<io.github.ugaikit.Values>)
