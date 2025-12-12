package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals

class CitationMetadataBuilderTest {
    @Test
    fun `test CitationMetadataBuilder`() {
        val citationMetadata =
            citationMetadata {
                citationSource {
                    startIndex = 0
                    endIndex = 10
                    uri = "https://example.com"
                    license = "Apache 2.0"
                }
            }

        assertEquals(1, citationMetadata.citationSources.size)
        assertEquals(0, citationMetadata.citationSources[0].startIndex)
        assertEquals(10, citationMetadata.citationSources[0].endIndex)
        assertEquals("https://example.com", citationMetadata.citationSources[0].uri)
        assertEquals("Apache 2.0", citationMetadata.citationSources[0].license)
    }
}
