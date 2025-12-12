package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals

class FileDataBuilderTest {
    @Test
    fun `build with all properties`() {
        val fileData =
            fileData {
                mimeType = "image/png"
                fileUri = "gs://bucket/image.png"
            }

        assertEquals("image/png", fileData.mimeType)
        assertEquals("gs://bucket/image.png", fileData.fileUri)
    }
}
