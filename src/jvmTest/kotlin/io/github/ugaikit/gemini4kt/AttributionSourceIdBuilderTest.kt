package io.github.ugaikit.gemini4kt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AttributionSourceIdBuilderTest {
    @Test
    fun `build with groundingPassage`() {
        val attributionSourceId =
            attributionSourceId {
                groundingPassage {
                    GroundingPassageId(passageId = "passage123", partIndex = 1)
                }
            }
        assertNotNull(attributionSourceId.groundingPassage)
        assertEquals("passage123", attributionSourceId.groundingPassage?.passageId)
        assertNull(attributionSourceId.semanticRetrieverChunk)
    }

    @Test
    fun `build with semanticRetrieverChunk`() {
        val attributionSourceId =
            attributionSourceId {
                semanticRetrieverChunk {
                    SemanticRetrieverChunk(source = "source123", chunk = "chunk content")
                }
            }
        assertNull(attributionSourceId.groundingPassage)
        assertNotNull(attributionSourceId.semanticRetrieverChunk)
        assertEquals("source123", attributionSourceId.semanticRetrieverChunk?.source)
    }

    @Test
    fun `build with both properties`() {
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
    fun `build with no properties`() {
        val attributionSourceId = attributionSourceId {}
        assertNull(attributionSourceId.groundingPassage)
        assertNull(attributionSourceId.semanticRetrieverChunk)
    }
}
