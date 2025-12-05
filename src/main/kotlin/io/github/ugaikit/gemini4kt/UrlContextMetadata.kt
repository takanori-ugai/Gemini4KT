package io.github.ugaikit.gemini4kt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Metadata about the URL context.
 *
 * @property urlMetadata List of metadata for each retrieved URL.
 */
@Serializable
data class UrlContextMetadata(
    @SerialName("url_metadata")
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
    @SerialName("retrieved_url")
    val retrievedUrl: String,
    @SerialName("url_retrieval_status")
    val urlRetrievalStatus: String,
)
