package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

@Serializable
data class AttributionSourceId(
    val groundingPassage: GroundingPassageId? = null,
    val semanticRetrieverChunk: SemanticRetrieverChunk? = null,
)
