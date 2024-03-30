package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class BatchEmbedRequest(val requests: List<io.github.ugaikit.gemini4kt.EmbedContentRequest>)
