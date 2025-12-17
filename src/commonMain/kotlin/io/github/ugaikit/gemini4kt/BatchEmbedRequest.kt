package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a batch request for embedding content.
 *
 * @property requests A list of [EmbedContentRequest] instances,
 * representing individual embedding requests to be processed in a batch.
 */
@Serializable
@JsExport
data class BatchEmbedRequest(
    val requests: Array<EmbedContentRequest>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BatchEmbedRequest

        if (!requests.contentEquals(other.requests)) return false

        return true
    }

    override fun hashCode(): Int {
        return requests.contentHashCode()
    }
}
