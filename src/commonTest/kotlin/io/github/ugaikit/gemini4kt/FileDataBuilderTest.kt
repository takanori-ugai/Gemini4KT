package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals

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
}
