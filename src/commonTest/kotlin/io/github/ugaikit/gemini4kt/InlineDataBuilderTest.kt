package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InlineDataBuilderTest {
    @Test
    fun buildWithAllProperties() {
        val inline =
            inlineData {
                mimeType { "image/png" }
                data { "base64-data" }
                displayName { "preview.png" }
            }

        assertEquals("image/png", inline.mimeType)
        assertEquals("base64-data", inline.data)
        assertEquals("preview.png", inline.displayName)
    }

    @Test
    fun buildRejectsMissingRequiredProperties() {
        val builder = InlineDataBuilder()

        val exception =
            assertFailsWith<IllegalArgumentException> {
                builder.build()
            }

        assertEquals("mimeType must be set before building InlineData.", exception.message)
    }
}
