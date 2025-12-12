package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class GroundingMetadataTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test serialization`() {
        val groundingMetadata =
            GroundingMetadata(
                searchEntryPoint = SearchEntryPoint(renderedContent = "<div>Content</div>"),
                webSearchQueries = listOf("query1", "query2"),
                groundingChunks =
                    listOf(
                        GroundingChunk(web = Web(uri = "http://example.com", title = "Example")),
                    ),
                groundingSupports =
                    listOf(
                        GroundingSupport(
                            segment = Segment(startIndex = 0, endIndex = 10, text = "Sometext"),
                            groundingChunkIndices = listOf(0),
                        ),
                    ),
            )
        val expectedJson =
            """
            {"searchEntryPoint":{"renderedContent":"<div>Content</div>"},"webSearchQueries":["query1","query2"],
            "groundingChunks":[{"web":{"uri":"http://example.com","title":"Example"}}],
            "groundingSupports":[{"segment":{"startIndex":0,"endIndex":10,"text":"Sometext"},"groundingChunkIndices":[0]}]}
            """.trimIndent().replace("\n", "").replace(" ", "")
        assertEquals(expectedJson, json.encodeToString(groundingMetadata))
    }

    @Test
    fun `test deserialization`() {
        val jsonString =
            """
            {
                "searchEntryPoint": {
                    "renderedContent": "<div>Content</div>"
                },
                "webSearchQueries": ["query1", "query2"],
                "groundingChunks": [
                    {
                        "web": {
                            "uri": "http://example.com",
                            "title": "Example"
                        }
                    }
                ],
                "groundingSupports": [
                    {
                        "segment": {
                            "startIndex": 0,
                            "endIndex": 10,
                            "text": "Some text"
                        },
                        "groundingChunkIndices": [0]
                    }
                ]
            }
            """.trimIndent()

        val groundingMetadata = json.decodeFromString<GroundingMetadata>(jsonString)

        assertEquals("<div>Content</div>", groundingMetadata.searchEntryPoint?.renderedContent)
        assertEquals(2, groundingMetadata.webSearchQueries.size)
        assertEquals("query1", groundingMetadata.webSearchQueries[0])
        assertEquals("query2", groundingMetadata.webSearchQueries[1])
        assertEquals(1, groundingMetadata.groundingChunks.size)
        assertEquals("http://example.com", groundingMetadata.groundingChunks[0].web?.uri)
        assertEquals("Example", groundingMetadata.groundingChunks[0].web?.title)
        assertEquals(1, groundingMetadata.groundingSupports.size)
        assertEquals(0, groundingMetadata.groundingSupports[0].segment?.startIndex)
        assertEquals(10, groundingMetadata.groundingSupports[0].segment?.endIndex)
        assertEquals("Some text", groundingMetadata.groundingSupports[0].segment?.text)
        assertEquals(1, groundingMetadata.groundingSupports[0].groundingChunkIndices.size)
        assertEquals(0, groundingMetadata.groundingSupports[0].groundingChunkIndices[0])
    }

    @Test
    fun `test deserialization with missing fields`() {
        val jsonString = "{}"

        val groundingMetadata = json.decodeFromString<GroundingMetadata>(jsonString)

        assertNull(groundingMetadata.searchEntryPoint)
        assertEquals(0, groundingMetadata.webSearchQueries.size)
        assertEquals(0, groundingMetadata.groundingChunks.size)
        assertEquals(0, groundingMetadata.groundingSupports.size)
    }
}
