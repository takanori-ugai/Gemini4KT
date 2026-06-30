package io.github.ugaikit.gemini4kt

import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.junit.jupiter.api.Assumptions
import java.io.File
import java.util.Base64
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Live integration coverage migrated from the old JVM main runner.
 */
class ITIntegrationTest {
    private val model = "gemma-4-31b-it"
    private val embedModel = "gemini-embedding-2"
    private val httpTooManyRequests = 429

    private fun getApiKey(): String? {
        var apiKey = System.getenv("GEMINI_API_KEY")
        if (apiKey == null) {
            apiKey =
                Gemini::class.java.getResourceAsStream("/prop.properties")?.use { inputStream ->
                    Properties()
                        .apply {
                            load(inputStream)
                        }.getProperty("apiKey")
                }
        }
        return apiKey
    }

    private fun handleQuotaError(error: GeminiException): Nothing {
        val isQuotaError =
            error.error.code == httpTooManyRequests || error.error.status == "RESOURCE_EXHAUSTED"
        val isTransientError =
            error.error.code >= 500 ||
                error.error.status == "INTERNAL" ||
                error.error.status == "UNAVAILABLE" ||
                error.error.status == "DEADLINE_EXCEEDED" ||
                error.message?.contains("internal error", ignoreCase = true) == true ||
                error.message?.contains("unavailable", ignoreCase = true) == true
        if (isQuotaError || isTransientError) {
            Assumptions.assumeTrue(false, "Skipping integration test due to quota exhaustion: ${error.error.message}")
        }
        throw error
    }

    private fun handleRequestTimeout(error: HttpRequestTimeoutException): Nothing {
        Assumptions.assumeTrue(false, "Skipping integration test due to request timeout: ${error.message}")
        throw error
    }

    @Test
    fun testContentAndEmbeddingApis() =
        runBlocking {
            val apiKey = getApiKey()
            Assumptions.assumeTrue(!apiKey.isNullOrEmpty(), "API key not found. Skipping integration test.")

            val gemini = Gemini(apiKey!!)
            try {
                val text = "Write a story about a magic backpack."
                val inputJson =
                    generateContentRequest {
                        content { part { text { text } } }
                        safetySetting {
                            category = HarmCategory.HARM_CATEGORY_HARASSMENT
                            threshold = Threshold.BLOCK_ONLY_HIGH
                        }
                    }

                val response = gemini.generateContent(inputJson, model = model)
                val textPart =
                    response.candidates
                        .firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull { it.text != null && it.thought != true }
                assertNotNull(textPart?.text)

                val tokenResponse =
                    gemini.countTokens(
                        CountTokensRequest(contents = listOf(Content(parts = arrayOf(Part(text))))),
                        model = model,
                    )
                assertTrue(tokenResponse.totalTokens > 0)

                val embedResponse =
                    gemini.embedContent(
                        EmbedContentRequest(
                            content = Content(parts = arrayOf(Part(text))),
                            model = "models/$embedModel",
                        ),
                        model = embedModel,
                    )
                assertTrue(embedResponse.embedding.values.isNotEmpty())

                val batchEmbedResponse =
                    gemini.batchEmbedContents(
                        BatchEmbedRequest(
                            requests =
                                listOf(
                                    EmbedContentRequest(
                                        content = Content(parts = arrayOf(Part(text))),
                                        model = "models/$embedModel",
                                    ),
                                ),
                        ),
                        model = embedModel,
                    )
                assertTrue(batchEmbedResponse.embeddings.isNotEmpty())
            } catch (error: GeminiException) {
                handleQuotaError(error)
            } catch (error: HttpRequestTimeoutException) {
                handleRequestTimeout(error)
            } finally {
                gemini.close()
            }
        }

    @Test
    fun testModelsAndMultimodalGeneration() =
        runBlocking {
            val apiKey = getApiKey()
            Assumptions.assumeTrue(!apiKey.isNullOrEmpty(), "API key not found. Skipping integration test.")

            val gemini = Gemini(apiKey!!)
            try {
                val models = gemini.getModels()
                assertTrue(models.models.isNotEmpty())

                val imagePath = Gemini::class.java.getResource("/scones.jpg")
                assertNotNull(imagePath)
                val image = File(imagePath.toURI())
                val base64Image = Base64.getEncoder().encodeToString(image.readBytes())

                val inputWithImage =
                    GenerateContentRequest(
                        contents =
                            arrayOf(
                                Content(
                                    parts =
                                        arrayOf(
                                            Part(text = "What is this picture?"),
                                            Part(
                                                inlineData =
                                                    InlineData(
                                                        mimeType = "image/jpeg",
                                                        data = base64Image,
                                                    ),
                                            ),
                                        ),
                                ),
                            ),
                    )

                val response = gemini.generateContent(inputWithImage, model)
                val textPart =
                    response.candidates
                        .firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull { it.text != null && it.thought != true }
                assertNotNull(textPart?.text)
            } catch (error: GeminiException) {
                handleQuotaError(error)
            } catch (error: HttpRequestTimeoutException) {
                handleRequestTimeout(error)
            } finally {
                gemini.close()
            }
        }

    @Test
    fun testFunctionCallingFirstTurn() =
        runBlocking {
            val apiKey = getApiKey()
            Assumptions.assumeTrue(!apiKey.isNullOrEmpty(), "API key not found. Skipping integration test.")

            val gemini = Gemini(apiKey!!)
            val tools = defineFunctionTools()
            val firstTurnRequest =
                GenerateContentRequest(
                    contents =
                        arrayOf(
                            content {
                                role = "user"
                                part {
                                    text {
                                        "Which theaters in Mountain View show Barbie movie?"
                                    }
                                }
                            },
                        ),
                    tools = tools,
                )

            try {
                val response = gemini.generateContent(firstTurnRequest, model)
                val parts =
                    response.candidates
                        .firstOrNull()
                        ?.content
                        ?.parts
                assertTrue(!parts.isNullOrEmpty())
            } catch (error: GeminiException) {
                handleQuotaError(error)
            } catch (error: HttpRequestTimeoutException) {
                handleRequestTimeout(error)
            } finally {
                gemini.close()
            }
        }

    @Test
    fun testFunctionCallingSecondTurnWithAllModelParts() =
        runBlocking {
            val apiKey = getApiKey()
            Assumptions.assumeTrue(!apiKey.isNullOrEmpty(), "API key not found. Skipping integration test.")

            val gemini = Gemini(apiKey!!)
            val tools = defineFunctionTools()
            val functionResponsePayload =
                buildJsonObject {
                    put("name", "the_theater")
                    putJsonObject("content") {
                        put("movie", "Barbie")
                        putJsonArray("theaters") {
                            addJsonObject {
                                put("name", "AMC Mountain View 16")
                                put("address", "2000 W El Camino Real, Mountain View, CA 94040")
                            }
                            addJsonObject {
                                put("name", "Regal Edwards 14")
                                put("address", "245 Castro St, Mountain View, CA 94040")
                            }
                        }
                    }
                }

            val firstTurnRequest =
                GenerateContentRequest(
                    contents =
                        arrayOf(
                            content {
                                role = "user"
                                part { text { "Which theaters in Mountain View show Barbie movie?" } }
                            },
                        ),
                    tools = tools,
                )

            try {
                val firstTurnResponse = gemini.generateContent(firstTurnRequest, model)
                val modelContent =
                    firstTurnResponse.candidates
                        .firstOrNull()
                        ?.content
                        ?: error("Model response is null in the first turn.")

                val secondTurnRequest =
                    GenerateContentRequest(
                        contents =
                            arrayOf(
                                content {
                                    role = "user"
                                    part { text { "Which theaters in Mountain View show Barbie movie?" } }
                                },
                                Content(
                                    parts = modelContent.parts,
                                    role = "model",
                                ),
                                content {
                                    role = "function"
                                    part {
                                        functionResponse {
                                            FunctionResponse(
                                                name = "find_theaters",
                                                response = functionResponsePayload,
                                            )
                                        }
                                    }
                                },
                            ),
                        tools = tools,
                    )

                val secondTurnResponse = gemini.generateContent(secondTurnRequest, model)
                val parts =
                    secondTurnResponse.candidates
                        .firstOrNull()
                        ?.content
                        ?.parts
                assertTrue(!parts.isNullOrEmpty())
            } catch (error: GeminiException) {
                handleQuotaError(error)
            } catch (error: HttpRequestTimeoutException) {
                handleRequestTimeout(error)
            } finally {
                gemini.close()
            }
        }

    private fun findMoviesFunction(): FunctionDeclaration =
        functionDeclaration {
            name = "find_movies"
            description =
                "find movie titles currently playing in theaters based on any description, genre, " +
                "title words, etc."
            parameters {
                type = "object"
                property("location") {
                    type = "string"
                    description = "The city and state, e.g. San Francisco, CA or a zip code e.g. 95616"
                }
                property("description") {
                    type = "string"
                    description = "Any kind of description including category or genre"
                }
                required("description")
            }
        }

    private fun findTheatersFunction(): FunctionDeclaration =
        functionDeclaration {
            name = "find_theaters"
            description =
                "find theaters based on location and optionally movie title which is currently " +
                "playing in theaters"
            parameters {
                type = "object"
                property("location") {
                    type = "string"
                    description = "The city and state, e.g. San Francisco, CA or a zip code e.g. 95616"
                }
                property("movie") {
                    type = "string"
                    description = "Any movie title"
                }
                required("location")
            }
        }

    private fun getShowtimesFunction(): FunctionDeclaration =
        FunctionDeclaration(
            name = "get_showtimes",
            description = "Find the start times for movies playing in a specific theater",
            parameters =
                Schema(
                    type = "object",
                    properties =
                        mapOf(
                            "location" to
                                Schema(
                                    type = "string",
                                    description = "The city and state, e.g. San Francisco, CA or a zip code e.g. 95616",
                                ),
                            "movie" to
                                Schema(
                                    type = "string",
                                    description = "Any movie title",
                                ),
                            "theater" to
                                Schema(
                                    type = "string",
                                    description = "Name of the theater",
                                ),
                            "date" to
                                Schema(
                                    type = "string",
                                    description = "Date for requested showtime",
                                ),
                        ),
                    required = listOf("location", "movie", "theater", "date"),
                ),
        )

    private fun defineFunctionTools(): Array<Tool> =
        arrayOf(
            Tool(
                functionDeclarations =
                    arrayOf(
                        findMoviesFunction(),
                        findTheatersFunction(),
                        getShowtimesFunction(),
                    ),
            ),
        )
}
