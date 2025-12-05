package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UrlContextTest {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @Test
    fun `test Tool serialization with urlContext`() {
        val tool = tool {
            urlContext()
        }
        assertNotNull(tool.urlContext)
        val jsonString = json.encodeToString(tool)
        // Verify that url_context is present and is an empty object
        assert(jsonString.contains("\"url_context\":{}"))
    }

    @Test
    fun `test Candidate deserialization with urlContextMetadata`() {
        val jsonString = """
            {
                "content": {
                    "parts": [{"text": "response text"}],
                    "role": "model"
                },
                "finishReason": "STOP",
                "index": 0,
                "url_context_metadata": {
                    "url_metadata": [
                        {
                            "retrieved_url": "https://example.com/recipe1",
                            "url_retrieval_status": "URL_RETRIEVAL_STATUS_SUCCESS"
                        },
                        {
                            "retrieved_url": "https://example.com/recipe2",
                            "url_retrieval_status": "URL_RETRIEVAL_STATUS_SUCCESS"
                        }
                    ]
                }
            }
        """.trimIndent()

        val candidate = json.decodeFromString<Candidate>(jsonString)

        assertNotNull(candidate.urlContextMetadata)
        assertEquals(2, candidate.urlContextMetadata!!.urlMetadata.size)

        val meta1 = candidate.urlContextMetadata!!.urlMetadata[0]
        assertEquals("https://example.com/recipe1", meta1.retrievedUrl)
        assertEquals("URL_RETRIEVAL_STATUS_SUCCESS", meta1.urlRetrievalStatus)

        val meta2 = candidate.urlContextMetadata!!.urlMetadata[1]
        assertEquals("https://example.com/recipe2", meta2.retrievedUrl)
        assertEquals("URL_RETRIEVAL_STATUS_SUCCESS", meta2.urlRetrievalStatus)
    }
}
