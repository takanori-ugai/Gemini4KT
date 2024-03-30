package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class BatchEmbedResponse(val embeddings: List<io.github.uaikit.Values>)
