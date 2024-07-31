package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class CachedContentList(
    val cachedContents: List<CachedContent>? = null,
    val nextPageToken: String? = null,
)
