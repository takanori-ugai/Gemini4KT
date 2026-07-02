package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Represents the file data builder test.
 */
class FileDataBuilderTest {
    /**
     * Handles build with all properties.
     */
    @Test
    fun buildWithAllProperties() {
        val fileData =
            fileData {
                mimeType = "image/png"
                fileUri = "gs://bucket/image.png"
            }

        assertEquals("image/png", fileData.mimeType)
        assertEquals("gs://bucket/image.png", fileData.fileUri)
    }

    @Test
    fun buildRejectsMissingRequiredProperties() {
        val builder = FileDataBuilder()

        val exception =
            assertFailsWith<IllegalArgumentException> {
                builder.build()
            }

        assertEquals("mimeType must be set before building FileData.", exception.message)
    }

    @Test
    fun buildRejectsMissingFileUri() {
        val builder = FileDataBuilder().apply { mimeType = "image/png" }

        val exception =
            assertFailsWith<IllegalArgumentException> {
                builder.build()
            }

        assertEquals("fileUri must be set before building FileData.", exception.message)
    }
}
