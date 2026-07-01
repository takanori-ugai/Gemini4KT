package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Represents the attribution source id builder test.
 */
class AttributionSourceIdBuilderTest {
    /**
     * Handles build with grounding passage.
     */
    @Test
    fun buildWithGroundingPassage() {
        val attributionSourceId =
            attributionSourceId {
                groundingPassage {
                    GroundingPassageId(passageId = "passage123", partIndex = 1)
                }
            }
        assertNotNull(attributionSourceId.groundingPassage)
        val groundingPassage = checkNotNull(attributionSourceId.groundingPassage)
        assertEquals("passage123", groundingPassage.passageId)
        assertNull(attributionSourceId.semanticRetrieverChunk)
    }

    /**
     * Handles build with semantic retriever chunk.
     */
    @Test
    fun buildWithSemanticRetrieverChunk() {
        val attributionSourceId =
            attributionSourceId {
                semanticRetrieverChunk {
                    SemanticRetrieverChunk(source = "source123", chunk = "chunk content")
                }
            }
        assertNull(attributionSourceId.groundingPassage)
        assertNotNull(attributionSourceId.semanticRetrieverChunk)
        val semanticRetrieverChunk = checkNotNull(attributionSourceId.semanticRetrieverChunk)
        assertEquals("source123", semanticRetrieverChunk.source)
    }

    /**
     * Handles build with both properties.
     */
    @Test
    fun buildWithBothProperties() {
        assertFailsWith<IllegalArgumentException> {
            attributionSourceId {
                groundingPassage {
                    GroundingPassageId(passageId = "passage123", partIndex = 1)
                }
                semanticRetrieverChunk {
                    SemanticRetrieverChunk(source = "source123", chunk = "chunk content")
                }
            }
        }
    }

    /**
     * Handles build with no properties.
     */
    @Test
    fun buildWithNoProperties() {
        assertFailsWith<IllegalArgumentException> {
            attributionSourceId {}
        }
    }
}
