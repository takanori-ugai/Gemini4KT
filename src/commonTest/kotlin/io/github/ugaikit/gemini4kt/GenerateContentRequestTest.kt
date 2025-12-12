package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.Test

class GenerateContentRequestTest {
    private val json = Json { prettyPrint = true }

    @Test
    fun `serialization with only contents`() {
        val request =
            GenerateContentRequest(
                contents =
                    listOf(
                        Content(
                            role = "user",
                            parts = listOf(Part(text = "Hello")),
                        ),
                    ),
            )
        val expectedJson = """{"contents":[{"parts":[{"text":"Hello"}],"role":"user"}]}"""
        val actualJson = json.encodeToString(request)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    private fun createFullRequest(): GenerateContentRequest =
        GenerateContentRequest(
            contents =
                listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = "How does this work?")),
                    ),
                ),
            tools =
                listOf(
                    Tool(
                        functionDeclarations =
                            listOf(
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
                            allowedFunctionNames = emptyList(),
                        ),
                ),
            safetySettings =
                listOf(
                    SafetySetting(
                        category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
                        threshold = Threshold.BLOCK_ONLY_HIGH,
                    ),
                ),
            systemInstruction =
                Content(
                    role = "system",
                    parts = listOf(Part(text = "You are a helpful assistant.")),
                ),
            generationConfig =
                GenerationConfig(
                    temperature = 0.9,
                    topK = 1,
                    topP = 1.0,
                    maxOutputTokens = 2048,
                    stopSequences = listOf("."),
                ),
            cachedContent = "cached-content-123",
        )

    @Test
    fun `serialization with all properties`() {
        val request = createFullRequest()

        val expectedJson =
            """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "How does this work?"
                    }
                  ],
                  "role": "user"
                }
              ],
              "tools": [
                {
                  "functionDeclarations": [
                    {
                      "name": "get_weather",
                      "description": "Returns the weather for a city.",
                      "parameters": {
                        "type": "OBJECT",
                        "properties": {
                          "city": {
                            "type": "STRING",
                            "description": "The city to get the weather for."
                          }
                        }
                      }
                    }
                  ]
                }
              ],
              "toolConfig": {
                "functionCallingConfig": {
                  "mode": "ANY",
                  "allowedFunctionNames": []
                }
              },
              "safetySettings": [
                {
                  "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                  "threshold": "BLOCK_ONLY_HIGH"
                }
              ],
              "system_instruction": {
                "parts": [
                  {
                    "text": "You are a helpful assistant."
                  }
                ],
                "role": "system"
              },
              "generationConfig": {
                "temperature": 0.9,
                "topK": 1,
                "topP": 1.0,
                "maxOutputTokens": 2048,
                "stopSequences": [
                  "."
                ]
              },
              "cachedContent": "cached-content-123"
            }
            """.trimIndent()
        val actualJson = json.encodeToString(request)
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }
}
