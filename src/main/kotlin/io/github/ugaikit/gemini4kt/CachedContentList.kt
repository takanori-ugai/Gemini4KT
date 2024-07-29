package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class CachedContentList(
    val cachedContents: List<CachedContent>,
    val nextPageToken: String,
)
