package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Represents the grounding metadata test.
 */
class GroundingMetadataTest {
    /**
     * Holds the json.
     */
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Tests test serialization.
     */
    @Test
    fun testSerialization() {
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

    /**
     * Tests test deserialization.
     */
    @Test
    fun testDeserialization() {
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

    /**
     * Tests test deserialization with missing fields.
     */
    @Test
    fun testDeserializationWithMissingFields() {
        val jsonString = "{}"

        val groundingMetadata = json.decodeFromString<GroundingMetadata>(jsonString)

        assertNull(groundingMetadata.searchEntryPoint)
        assertEquals(0, groundingMetadata.webSearchQueries.size)
        assertEquals(0, groundingMetadata.groundingChunks.size)
        assertEquals(0, groundingMetadata.groundingSupports.size)
    }

    /**
     * Tests grounding-related model serialization and enum reachability.
     */
    @Test
    fun testGroundingRelatedModelSerializationAndEnums() {
        val metadata =
            GroundingMetadata(
                groundingChunks =
                    listOf(
                        GroundingChunk(
                            image =
                                Image(
                                    sourceUri = "https://source.example.com",
                                    imageUri = "https://img.example.com/1.png",
                                    title = "Image title",
                                    domain = "example.com",
                                ),
                            retrievedContext =
                                RetrievedContext(
                                    customMetadata =
                                        listOf(
                                            CustomMetadata(
                                                key = "tags",
                                                stringListValue = StringList(values = arrayOf("kotlin", "sdk")),
                                            ),
                                        ),
                                    uri = "gs://doc/1",
                                    title = "Doc title",
                                    text = "Doc text",
                                    fileSearchStore = "store-a",
                                ),
                            maps =
                                Maps(
                                    uri = "https://maps.example.com/place",
                                    title = "Cafe",
                                    text = "Open now",
                                    placeId = "places/123",
                                    placeAnswerSources =
                                        PlaceAnswerSources(
                                            reviewSnippets =
                                                listOf(
                                                    ReviewSnippet(
                                                        reviewId = "r-1",
                                                        googleMapsUri = "https://maps.google.com/review/r-1",
                                                        title = "Great place",
                                                    ),
                                                ),
                                        ),
                                ),
                        ),
                    ),
                retrievalMetadata = RetrievalMetadata(googleSearchDynamicRetrievalScore = 0.77),
            )
        val status =
            ModelStatus(
                modelStage = ModelStage.STABLE,
                retirementTime = "2099-01-01T00:00:00Z",
                message = "Stable model",
            )

        val metadataEncoded = json.encodeToString(metadata)
        val metadataDecoded = json.decodeFromString<GroundingMetadata>(metadataEncoded)
        val statusEncoded = json.encodeToString(status)
        val statusDecoded = json.decodeFromString<ModelStatus>(statusEncoded)

        assertEquals(
            "kotlin",
            metadataDecoded.groundingChunks[0]
                .retrievedContext
                ?.customMetadata
                ?.first()
                ?.stringListValue
                ?.values
                ?.first(),
        )
        assertEquals(
            "r-1",
            metadataDecoded.groundingChunks[0]
                .maps
                ?.placeAnswerSources
                ?.reviewSnippets
                ?.first()
                ?.reviewId,
        )
        assertEquals(0.77, metadataDecoded.retrievalMetadata?.googleSearchDynamicRetrievalScore)
        assertEquals(ModelStage.STABLE, statusDecoded.modelStage)
        assertEquals(true, ToolType.entries.isNotEmpty())
        assertEquals(true, ThinkingLevel.entries.isNotEmpty())
        assertEquals(true, Environment.entries.isNotEmpty())
    }
}
