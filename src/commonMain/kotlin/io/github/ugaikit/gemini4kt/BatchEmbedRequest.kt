package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a batch request for embedding content.
 *
 * @property requests A list of [EmbedContentRequest] instances,
 * representing individual embedding requests to be processed in a batch.
 */
@Serializable
data class BatchEmbedRequest(
    val requests: List<EmbedContentRequest>,
)
