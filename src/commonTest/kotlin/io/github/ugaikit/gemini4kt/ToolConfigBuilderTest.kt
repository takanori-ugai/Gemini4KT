package io.github.ugaikit.gemini4kt

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ToolConfigBuilderTest {
    @Test
    fun `build with functionCallingConfig`() {
        val toolConfig =
            toolConfig {
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
        val toolConfig =
            toolConfig {
                functionCallingConfig {
                    mode = Mode.ANY
                }
                retrievalConfig =
                    RetrievalConfig(
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
        val toolConfig =
            toolConfig {
                functionCallingConfig {
                    mode = Mode.ANY
                    allowFunction("search")
                    allowFunction("translate")
                }
                retrievalConfig =
                    RetrievalConfig(
                        latLng = LatLng(latitude = 34.0522, longitude = -118.2437),
                        languageCode = "en-US",
                    )
            }

        assertNotNull(toolConfig.functionCallingConfig)
        assertNotNull(toolConfig.retrievalConfig)
    }

    @Test
    fun `build without functionCallingConfig throws exception`() {
        assertFailsWith<IllegalStateException> {
            toolConfig {
                retrievalConfig =
                    RetrievalConfig(
                        latLng = LatLng(latitude = 34.0522, longitude = -118.2437),
                        languageCode = "en-US",
                    )
            }
        }
    }
}
