package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AttributionSourceIdTest {
    @Test
    fun `test default constructor`() {
        val attributionSourceId = AttributionSourceId()
        assertNull(attributionSourceId.groundingPassage)
        assertNull(attributionSourceId.semanticRetrieverChunk)
    }

    @Test
    fun `test constructor with groundingPassage`() {
        val groundingPassage = GroundingPassageId(passageId = "passage123", partIndex = 1)
        val attributionSourceId = AttributionSourceId(groundingPassage = groundingPassage)
        assertEquals(groundingPassage, attributionSourceId.groundingPassage)
        assertNull(attributionSourceId.semanticRetrieverChunk)
    }

    @Test
    fun `test constructor with semanticRetrieverChunk`() {
        val semanticRetrieverChunk = SemanticRetrieverChunk(source = "source123", chunk = "chunk content")
        val attributionSourceId = AttributionSourceId(semanticRetrieverChunk = semanticRetrieverChunk)
        assertNull(attributionSourceId.groundingPassage)
        assertEquals(semanticRetrieverChunk, attributionSourceId.semanticRetrieverChunk)
    }

    @Test
    fun `test constructor with both properties`() {
        val groundingPassage = GroundingPassageId(passageId = "passage123", partIndex = 1)
        val semanticRetrieverChunk = SemanticRetrieverChunk(source = "source123", chunk = "chunk content")
        val attributionSourceId =
            AttributionSourceId(
                groundingPassage = groundingPassage,
                semanticRetrieverChunk = semanticRetrieverChunk,
            )
        assertEquals(groundingPassage, attributionSourceId.groundingPassage)
        assertEquals(semanticRetrieverChunk, attributionSourceId.semanticRetrieverChunk)
    }

    @Test
    fun `test JSON serialization and deserialization`() {
        val groundingPassage = GroundingPassageId(passageId = "passage123", partIndex = 1)
        val semanticRetrieverChunk = SemanticRetrieverChunk(source = "source123", chunk = "chunk content")
        val attributionSourceId =
            AttributionSourceId(
                groundingPassage = groundingPassage,
                semanticRetrieverChunk = semanticRetrieverChunk,
            )

        val json = Json.encodeToString(attributionSourceId)
        val deserialized = Json.decodeFromString<AttributionSourceId>(json)

        assertEquals(attributionSourceId, deserialized)
    }
}
