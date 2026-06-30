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

/**
 * Represents the attribution source id builder.
 */
class AttributionSourceIdBuilder {
    /**
     * Holds the grounding passage.
     */
    var groundingPassage: GroundingPassageId? = null

    /**
     * Holds the semantic retriever chunk.
     */
    var semanticRetrieverChunk: SemanticRetrieverChunk? = null

    /**
     * Handles grounding passage.
     *
     * @param init The init.
     */
    fun groundingPassage(init: () -> GroundingPassageId?) {
        groundingPassage = init()
    }

    /**
     * Handles semantic retriever chunk.
     *
     * @param init The init.
     */
    fun semanticRetrieverChunk(init: () -> SemanticRetrieverChunk?) {
        semanticRetrieverChunk = init()
    }

    /**
     * Handles build.
     */
    fun build() =
        AttributionSourceId(groundingPassage, semanticRetrieverChunk).also {
            require(listOfNotNull(groundingPassage, semanticRetrieverChunk).size <= 1) {
                "AttributionSourceIdBuilder supports only one attribution source at a time."
            }
        }
}

/**
 * Handles attribution source id.
 *
 * @param init The init.
 */
fun attributionSourceId(init: AttributionSourceIdBuilder.() -> Unit): AttributionSourceId =
    AttributionSourceIdBuilder()
        .apply(init)
        .build()
