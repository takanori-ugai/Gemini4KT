package io.github.ugaikit.gemini4kt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GenerateContentRequestBuilderTest {
    private fun buildFullRequest(): GenerateContentRequest =
        generateContentRequest {
            content {
                role = "user"
                part { text { "Hello" } }
            }
            tool {
                functionDeclaration {
                    name = "get_weather"
                    description = "Returns the weather for a city."
                    parameters {
                        type = "OBJECT"
                        property("city") {
                            type = "STRING"
                            description = "The city to get the weather for."
                        }
                    }
                }
            }
            toolConfig {
                functionCallingConfig {
                    mode = Mode.ANY
                }
            }
            safetySetting {
                category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
                threshold = Threshold.BLOCK_ONLY_HIGH
            }
            systemInstruction {
                role = "system"
                part { text { "You are a helpful assistant." } }
            }
            generationConfig {
                temperature = 0.9
                topK = 1
                topP = 1.0
                maxOutputTokens = 2048
                stopSequence(".")
            }
            cachedContent = "cached-content-123"
        }

    @Test
    fun `build with all properties`() {
        val request = buildFullRequest()

        assertEquals(1, request.contents.size)
        assertEquals("user", request.contents[0].role)
        assertEquals("Hello", request.contents[0].parts[0].text)
        assertEquals(1, request.tools.size)
        assertEquals(
            "get_weather",
            request.tools[0]
                .functionDeclarations
                ?.get(0)
                ?.name,
        )
        assertNotNull(request.toolConfig)
        assertEquals(Mode.ANY, request.toolConfig?.functionCallingConfig?.mode)
        assertEquals(1, request.safetySettings.size)
        assertEquals(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, request.safetySettings[0].category)
        assertNotNull(request.systemInstruction)
        assertEquals("system", request.systemInstruction?.role)
        assertNotNull(request.generationConfig)
        assertEquals(0.9, request.generationConfig?.temperature)
        assertEquals("cached-content-123", request.cachedContent)
    }

    @Test
    fun `build with only required properties`() {
        val request =
            generateContentRequest {
                content {
                    part { text { "Hello" } }
                }
            }

        assertEquals(1, request.contents.size)
        assertTrue(request.tools.isEmpty())
        assertNull(request.toolConfig)
        assertTrue(request.safetySettings.isEmpty())
        assertNull(request.systemInstruction)
        assertNull(request.generationConfig)
        assertNull(request.cachedContent)
    }

    @Test
    fun `build with multiple items in lists`() {
        val request =
            generateContentRequest {
                content {
                    part { text { "Content 1" } }
                }
                content {
                    part { text { "Content 2" } }
                }
                tool {
                    functionDeclaration {
                        name = "tool1"
                        description = "description for tool1"
                        parameters {
                            type = "OBJECT"
                        }
                    }
                }
                tool {
                    functionDeclaration {
                        name = "tool2"
                        description = "description for tool2"
                        parameters {
                            type = "OBJECT"
                        }
                    }
                }
                safetySetting {
                    category = HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT
                    threshold = Threshold.BLOCK_ONLY_HIGH
                }
                safetySetting {
                    category = HarmCategory.HARM_CATEGORY_HARASSMENT
                    threshold = Threshold.BLOCK_MEDIUM_AND_ABOVE
                }
            }

        assertEquals(2, request.contents.size)
        assertEquals(2, request.tools.size)
        assertEquals(2, request.safetySettings.size)
    }
}
