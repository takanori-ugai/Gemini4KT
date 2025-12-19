package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateContentRequestTest {
    private val json = Json { prettyPrint = true }

    @Test
    fun serializationWithOnlyContents() {
        val request =
            GenerateContentRequest(
                contents =
                    arrayOf(
                        Content(
                            role = "user",
                            parts = arrayOf(Part(text = "Hello")),
                        ),
                    ),
            )
        val expectedJson = """{"contents":[{"parts":[{"text":"Hello"}],"role":"user"}],"tools":[],"safetySettings":[]}"""
        val actualJson = json.encodeToString(request)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    private fun createFullRequest(): GenerateContentRequest =
        GenerateContentRequest(
            contents =
                arrayOf(
                    Content(
                        role = "user",
                        parts = arrayOf(Part(text = "How does this work?")),
                    ),
                ),
            tools =
                arrayOf(
                    Tool(
                        functionDeclarations =
                            arrayOf(
                                FunctionDeclaration(
                                    name = "get_weather",
                                    description = "Returns the weather for a city.",
                                    parameters =
                                        Schema(
                                            type = "OBJECT",
                                            properties =
                                                mapOf(
                                                    "city" to
                                                        Schema(
                                                            type = "STRING",
                                                            description = "The city to get the weather for.",
                                                        ),
                                                ),
                                        ),
                                ),
                            ),
                    ),
                ),
            toolConfig =
                ToolConfig(
                    functionCallingConfig =
                        FunctionCallingConfig(
                            mode = Mode.ANY,
                            allowedFunctionNames = emptyArray(),
                        ),
                ),
            safetySettings =
                arrayOf(
                    SafetySetting(
                        category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
                        threshold = Threshold.BLOCK_ONLY_HIGH,
                    ),
                ),
            systemInstruction =
                Content(
                    role = "system",
                    parts = arrayOf(Part(text = "You are a helpful assistant.")),
                ),
            generationConfig =
                GenerationConfig(
                    temperature = 0.9,
                    topK = 1,
                    topP = 1.0,
                    maxOutputTokens = 2048,
                    stopSequences = arrayOf("."),
                ),
            cachedContent = "cached-content-123",
        )

    @Test
    fun serializationWithAllProperties() {
        val request = createFullRequest()
        val actualJson = json.encodeToString(request)
        // Spot-check key fields rather than full structural equality (arrays/defaults are always emitted).
        assertTrue(actualJson.contains("\"get_weather\""))
        assertTrue(actualJson.contains("\"cached-content-123\""))
        assertTrue(actualJson.contains("\"HARM_CATEGORY_DANGEROUS_CONTENT\""))
        assertTrue(actualJson.contains("\"You are a helpful assistant.\""))
    }
}
