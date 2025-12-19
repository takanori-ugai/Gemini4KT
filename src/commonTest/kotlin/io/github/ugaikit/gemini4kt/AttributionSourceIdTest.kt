package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AttributionSourceIdTest {
    @Test
    fun testDefaultConstructor() {
        val attributionSourceId = AttributionSourceId()
        assertNull(attributionSourceId.groundingPassage)
        assertNull(attributionSourceId.semanticRetrieverChunk)
    }

    @Test
    fun testConstructorWithGroundingPassage() {
        val groundingPassage = GroundingPassageId(passageId = "passage123", partIndex = 1)
        val attributionSourceId = AttributionSourceId(groundingPassage = groundingPassage)
        assertEquals(groundingPassage, attributionSourceId.groundingPassage)
        assertNull(attributionSourceId.semanticRetrieverChunk)
    }

    @Test
    fun testConstructorWithSemanticRetrieverChunk() {
        val semanticRetrieverChunk = SemanticRetrieverChunk(source = "source123", chunk = "chunk content")
        val attributionSourceId = AttributionSourceId(semanticRetrieverChunk = semanticRetrieverChunk)
        assertNull(attributionSourceId.groundingPassage)
        assertEquals(semanticRetrieverChunk, attributionSourceId.semanticRetrieverChunk)
    }

    @Test
    fun testConstructorWithBothProperties() {
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
    fun testJSONSerializationAndDeserialization() {
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
