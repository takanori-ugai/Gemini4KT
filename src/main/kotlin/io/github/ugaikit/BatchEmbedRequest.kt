package io.github.ugaikit

import kotlinx.serialization.Serializable

@Serializable
data class BatchEmbedRequest(val requests: List<EmbedContentRequest>)
