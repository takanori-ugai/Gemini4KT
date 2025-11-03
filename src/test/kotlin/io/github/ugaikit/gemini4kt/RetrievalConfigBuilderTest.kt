package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals

class RetrievalConfigBuilderTest {
    @Test
    fun `test RetrievalConfigBuilder`() {
        val retrievalConfig = retrievalConfig {
            latLng = LatLng(latitude = 35.6895, longitude = 139.6917)
            languageCode = "en-US"
        }

        assertEquals(35.6895, retrievalConfig.latLng?.latitude)
        assertEquals(139.6917, retrievalConfig.latLng?.longitude)
        assertEquals("en-US", retrievalConfig.languageCode)
    }
}
