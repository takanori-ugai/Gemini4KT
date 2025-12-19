package io.github.ugaikit.gemini4kt

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PartBuilderTest {
    private val image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="

    @Test
    fun `build with text`() {
        val part =
            part {
                text { "Hello" }
            }
        assertEquals("Hello", part.text)
        assertNull(part.inlineData)
        assertNull(part.functionCall)
        assertNull(part.functionResponse)
        assertNull(part.fileData)
    }

    @Test
    fun `build with inlineData`() {
        val part =
            part {
                inlineData {
                    mimeType { "image/png" }
                    data { image }
                }
            }
        assertNull(part.text)
        assertNotNull(part.inlineData)
        val inlineData = checkNotNull(part.inlineData)
        assertEquals("image/png", inlineData.mimeType)
    }

    @Test
    fun `build with functionCall`() {
        val part =
            part {
                functionCall {
                    name = "get_weather"
                    arg("city", Json.parseToJsonElement("\"New York\""))
                }
            }
        assertNull(part.text)
        assertNotNull(part.functionCall)
        val functionCall = checkNotNull(part.functionCall)
        assertEquals("get_weather", functionCall.name)
    }

    @Test
    fun `build with functionResponse`() {
        val part =
            part {
                functionResponse {
                    FunctionResponse(
                        name = "get_weather",
                        response = JsonObject(mapOf("weather" to Json.parseToJsonElement("\"sunny\""))),
                    )
                }
            }
        assertNull(part.text)
        assertNotNull(part.functionResponse)
        val functionResponse = checkNotNull(part.functionResponse)
        assertEquals("get_weather", functionResponse.name)
    }

    @Test
    fun `build with fileData`() {
        val part =
            part {
                fileData {
                    FileData(
                        mimeType = "image/png",
                        fileUri = "gs://bucket/image.png",
                    )
                }
            }
        assertNull(part.text)
        assertNotNull(part.fileData)
        val fileData = checkNotNull(part.fileData)
        assertEquals("image/png", fileData.mimeType)
    }
}
