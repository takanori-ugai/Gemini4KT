package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class BatchEmbedRequest(val requests: List<EmbedContentRequest>)
