package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.Test

class ToolConfigTest {
    @Test
    fun `ToolConfig serializes correctly with retrievalConfig`() {
        val latLng = LatLng(latitude = 34.0522, longitude = -118.2437)
        val retrievalConfig = RetrievalConfig(latLng = latLng, languageCode = "en-US")
        val functionCallingConfig =
            FunctionCallingConfig(
                mode = Mode.ANY,
                allowedFunctionNames = listOf("search", "translate"),
            )
        val toolConfig =
            ToolConfig(
                functionCallingConfig = functionCallingConfig,
                retrievalConfig = retrievalConfig,
            )

        val json = Json

        val actualJsonString = json.encodeToString(toolConfig)
        val expectedJsonString =
            """
            {"functionCallingConfig":{"mode":"ANY","allowedFunctionNames":["search","translate"]},
            "retrievalConfig":{"latLng":{"latitude":34.0522,"longitude":-118.2437},
            "languageCode":"en-US"}}
            """.trimIndent().replace("\n", "")

        assertEquals(
            Json.parseToJsonElement(expectedJsonString),
            Json.parseToJsonElement(actualJsonString),
        )
    }
}
