package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class CitationMetadata(
    val citationSources: List<CitationSource>,
)
