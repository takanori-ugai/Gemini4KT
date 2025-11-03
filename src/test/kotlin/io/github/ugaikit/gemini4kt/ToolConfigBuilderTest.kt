package io.github.ugaikit.gemini4kt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ToolConfigBuilderTest {

    @Test
    fun `build with functionCallingConfig`() {
        val toolConfig = toolConfig {
            functionCallingConfig {
                mode = Mode.ANY
                allowFunction("search")
                allowFunction("translate")
            }
        }

        assertNotNull(toolConfig.functionCallingConfig)
        assertEquals(Mode.ANY, toolConfig.functionCallingConfig?.mode)
        assertNull(toolConfig.retrievalConfig)
    }

    @Test
    fun `build with retrievalConfig`() {
        val toolConfig = toolConfig {
            functionCallingConfig {
                mode = Mode.ANY
            }
            retrievalConfig = RetrievalConfig(
                latLng = LatLng(latitude = 34.0522, longitude = -118.2437),
                languageCode = "en-US",
            )
        }

        assertNotNull(toolConfig.retrievalConfig)
        assertEquals(34.0522, toolConfig.retrievalConfig?.latLng?.latitude)
        assertNotNull(toolConfig.functionCallingConfig)
    }

    @Test
    fun `build with both properties`() {
        val toolConfig = toolConfig {
            functionCallingConfig {
                mode = Mode.ANY
                allowFunction("search")
                allowFunction("translate")
            }
            retrievalConfig = RetrievalConfig(
                latLng = LatLng(latitude = 34.0522, longitude = -118.2437),
                languageCode = "en-US",
            )
        }

        assertNotNull(toolConfig.functionCallingConfig)
        assertNotNull(toolConfig.retrievalConfig)
    }

    @Test
    fun `build without functionCallingConfig throws exception`() {
        assertThrows<IllegalStateException> {
            toolConfig {
                retrievalConfig = RetrievalConfig(
                    latLng = LatLng(latitude = 34.0522, longitude = -118.2437),
                    languageCode = "en-US",
                )
            }
        }
    }
}
