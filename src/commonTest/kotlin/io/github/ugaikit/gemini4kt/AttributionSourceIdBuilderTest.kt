package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AttributionSourceIdBuilderTest {
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

    @Test
    fun buildWithBothProperties() {
        val attributionSourceId =
            attributionSourceId {
                groundingPassage {
                    GroundingPassageId(passageId = "passage123", partIndex = 1)
                }
                semanticRetrieverChunk {
                    SemanticRetrieverChunk(source = "source123", chunk = "chunk content")
                }
            }
        assertNotNull(attributionSourceId.groundingPassage)
        assertNotNull(attributionSourceId.semanticRetrieverChunk)
    }

    @Test
    fun buildWithNoProperties() {
        val attributionSourceId = attributionSourceId {}
        assertNull(attributionSourceId.groundingPassage)
        assertNull(attributionSourceId.semanticRetrieverChunk)
    }
}
