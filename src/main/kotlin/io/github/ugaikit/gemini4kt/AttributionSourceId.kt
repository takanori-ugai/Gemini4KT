package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Identifies the source of an attribution, which can be either a grounding
 * passage or a chunk retrieved by a semantic retriever. This class allows for
 * specifying the origin of content or data used in processing or analysis.
 *
 * @property groundingPassage An optional [GroundingPassageId] that identifies a
 * specific passage used for grounding information or context. This is nullable
 * to accommodate situations where the attribution does not originate from a
 * grounding passage.
 * @property semanticRetrieverChunk An optional [SemanticRetrieverChunk] that
 * identifies a specific chunk of content retrieved by a semantic retrieval
 * process. This is nullable to accommodate situations where the attribution does
 * not originate from a semantic retriever chunk.
 */
@Serializable
data class AttributionSourceId(
    val groundingPassage: GroundingPassageId? = null,
    val semanticRetrieverChunk: SemanticRetrieverChunk? = null,
)
