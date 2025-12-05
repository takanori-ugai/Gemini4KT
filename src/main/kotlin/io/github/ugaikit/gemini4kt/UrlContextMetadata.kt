package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Metadata about the URL context.
 *
 * @property urlMetadata List of metadata for each retrieved URL.
 */
@Serializable
data class UrlContextMetadata(
    val urlMetadata: List<UrlMetadata> = emptyList(),
)

/**
 * Metadata for a single retrieved URL.
 *
 * @property retrievedUrl The URL that was retrieved.
 * @property urlRetrievalStatus The status of the URL retrieval.
 */
@Serializable
data class UrlMetadata(
    val retrievedUrl: String,
    val urlRetrievalStatus: String,
)
