package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class CitationSource(
    val startIndex: Int,
    val endIndex: Int,
    val uri: String,
    val license: String,
)
