package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UrlContextTest {
    private val json =
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    @Test
    fun testToolSerializationWithUrlContext() {
        val tool =
            tool {
                urlContext()
            }
        assertNotNull(tool.urlContext)
        val jsonString = json.encodeToString(tool)
        // Verify that url_context is present and is an empty object
        assertTrue(jsonString.contains("\"url_context\":{}"))
    }

    @Test
    fun testCandidateDeserializationWithUrlContextMetadata() {
        val jsonString =
            """
            {
                "content": {
                    "parts": [{"text": "response text"}],
                    "role": "model"
                },
                "finishReason": "STOP",
                "index": 0,
                "urlContextMetadata": {
                    "urlMetadata": [
                        {
                            "retrievedUrl": "https://example.com/recipe1",
                            "urlRetrievalStatus": "URL_RETRIEVAL_STATUS_SUCCESS"
                        },
                        {
                            "retrievedUrl": "https://example.com/recipe2",
                            "urlRetrievalStatus": "URL_RETRIEVAL_STATUS_SUCCESS"
                        }
                    ]
                }
            }
            """.trimIndent()

        val candidate = json.decodeFromString<Candidate>(jsonString)

        assertNotNull(candidate.urlContextMetadata)
        val urlContextMetadata = checkNotNull(candidate.urlContextMetadata)
        assertEquals(2, urlContextMetadata.urlMetadata.size)

        val meta1 = urlContextMetadata.urlMetadata[0]
        assertEquals("https://example.com/recipe1", meta1.retrievedUrl)
        assertEquals("URL_RETRIEVAL_STATUS_SUCCESS", meta1.urlRetrievalStatus)

        val meta2 = urlContextMetadata.urlMetadata[1]
        assertEquals("https://example.com/recipe2", meta2.retrievedUrl)
        assertEquals("URL_RETRIEVAL_STATUS_SUCCESS", meta2.urlRetrievalStatus)
    }
}
