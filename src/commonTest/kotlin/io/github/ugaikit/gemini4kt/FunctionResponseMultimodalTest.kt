package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionResponseMultimodalTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            prettyPrint = false
        }

    @Test
    fun functionResponseSupportsMultimodalParts() {
        val imagePart =
            Part(
                inlineData =
                    InlineData(
                        mimeType = "image/jpeg",
                        data = "base64data",
                        displayName = "instrument.jpg",
                    ),
            )
        val functionResponse =
            FunctionResponse(
                name = "get_image",
                response =
                    buildJsonObject {
                        put("\$ref", "instrument.jpg")
                    },
                parts = arrayOf(imagePart),
            )
        val part = Part(functionResponse = functionResponse)
        val encoded = json.encodeToString(part)
        val decodedPart: Part = json.decodeFromString(encoded)
        val fnResponse = decodedPart.functionResponse
        requireNotNull(fnResponse)
        assertEquals("get_image", fnResponse.name)
        assertEquals(
            buildJsonObject {
                put("\$ref", "instrument.jpg")
            },
            fnResponse.response,
        )

        val inlineData = fnResponse.parts?.firstOrNull()?.inlineData
        requireNotNull(inlineData)
        assertEquals("image/jpeg", inlineData.mimeType)
        assertEquals("base64data", inlineData.data)
        assertEquals("instrument.jpg", inlineData.displayName)
    }

    @Test
    fun inlineDataBuilderSupportsDisplayName() {
        val inline =
            inlineData {
                mimeType { "image/png" }
                data { "img" }
                displayName { "preview.png" }
            }
        val encoded = json.encodeToString(inline)
        val expected = """{"mimeType":"image/png","data":"img","displayName":"preview.png"}"""
        assertEquals(expected, encoded)
    }
}
