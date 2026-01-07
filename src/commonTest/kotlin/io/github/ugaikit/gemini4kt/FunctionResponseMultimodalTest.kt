package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
        val decoded: JsonElement = json.decodeFromString(encoded)
        val decodedObject = decoded.jsonObject
        val fn = decodedObject["functionResponse"]!!.jsonObject
        assertEquals("get_image", fn["name"]!!.jsonPrimitive.content)
        assertEquals("instrument.jpg", fn["response"]!!.jsonObject["\$ref"]!!.jsonPrimitive.content)
        val inline = fn["parts"]!!.jsonArray[0].jsonObject["inlineData"]!!.jsonObject
        assertEquals("image/jpeg", inline["mimeType"]!!.jsonPrimitive.content)
        assertEquals("base64data", inline["data"]!!.jsonPrimitive.content)
        assertEquals("instrument.jpg", inline["displayName"]!!.jsonPrimitive.content)
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
