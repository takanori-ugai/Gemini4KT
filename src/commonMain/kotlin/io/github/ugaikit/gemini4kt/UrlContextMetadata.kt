package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Metadata about the URL context.
 *
 * @property urlMetadata List of metadata for each retrieved URL.
 */
@Serializable
@JsExport
data class UrlContextMetadata(
    val urlMetadata: Array<UrlMetadata> = emptyArray(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UrlContextMetadata

        if (!urlMetadata.contentEquals(other.urlMetadata)) return false

        return true
    }

    override fun hashCode(): Int {
        return urlMetadata.contentHashCode()
    }
}

/**
 * Metadata for a single retrieved URL.
 *
 * @property retrievedUrl The URL that was retrieved.
 * @property urlRetrievalStatus The status of the URL retrieval.
 */
@Serializable
@JsExport
data class UrlMetadata(
    val retrievedUrl: String,
    val urlRetrievalStatus: String,
)
