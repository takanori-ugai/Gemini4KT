package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the response for a batch embedding request.
 *
 * @property embeddings A list of [Values] objects, each containing the embedding results
 * for a corresponding request in the batch.
 */
@Serializable
data class BatchEmbedResponse(
    val embeddings: List<Values>,
)
