package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals

class CitationSourceBuilderTest {
    @Test
    fun `test CitationSourceBuilder`() {
        val citationSource = citationSource {
            startIndex = 0
            endIndex = 10
            uri = "https://example.com"
            license = "Apache 2.0"
        }

        assertEquals(0, citationSource.startIndex)
        assertEquals(10, citationSource.endIndex)
        assertEquals("https://example.com", citationSource.uri)
        assertEquals("Apache 2.0", citationSource.license)
    }
}
