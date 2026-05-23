package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Represents the tool config builder test.
 */
class ToolConfigBuilderTest {
    /**
     * Handles build with function calling config.
     */
    @Test
    fun buildWithFunctionCallingConfig() {
        val toolConfig =
            toolConfig {
                functionCallingConfig {
                    mode = Mode.ANY
                    allowFunction("search")
                    allowFunction("translate")
                }
            }

        assertNotNull(toolConfig.functionCallingConfig)
        val functionCallingConfig = checkNotNull(toolConfig.functionCallingConfig)
        assertEquals(Mode.ANY, functionCallingConfig.mode)
        assertNull(toolConfig.retrievalConfig)
    }

    /**
     * Handles build with retrieval config.
     */
    @Test
    fun buildWithRetrievalConfig() {
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
        val retrievalConfig = checkNotNull(toolConfig.retrievalConfig)
        assertEquals(34.0522, retrievalConfig.latLng?.latitude)
        checkNotNull(toolConfig.functionCallingConfig)
    }

    /**
     * Handles build with both properties.
     */
    @Test
    fun buildWithBothProperties() {
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

    /**
     * Handles build without function calling config does not throw.
     */
    @Test
    fun buildWithoutFunctionCallingConfigDoesNotThrow() {
        val toolConfig =
            toolConfig {
                retrievalConfig =
                    RetrievalConfig(
                        latLng = LatLng(latitude = 34.0522, longitude = -118.2437),
                        languageCode = "en-US",
                    )
            }
        assertNull(toolConfig.functionCallingConfig)
        assertNotNull(toolConfig.retrievalConfig)
    }
}
