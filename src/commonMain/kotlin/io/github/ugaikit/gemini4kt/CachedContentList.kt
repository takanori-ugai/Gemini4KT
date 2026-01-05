package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the cached content list.
 *
 * @property cachedContents The cached contents.
 * @property nextPageToken The next page token.
 */
@Serializable
data class CachedContentList(
    val cachedContents: List<CachedContent>? = null,
    val nextPageToken: String? = null,
)
