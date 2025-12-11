@file:Suppress("TooManyFunctions")

package io.github.ugaikit.gemini4kt

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.io.File
import java.util.Base64
import java.util.Properties

private const val REPEAT_COUNT = 10000
private const val EMBED_MODEL = "text-embedding-004"
private const val FLASH_MODEL = "gemini-2.5-flash-lite"
private const val PRO_MODEL = "gemini-2.5-pro"

private suspend fun testContentGeneration(gemini: Gemini) {
    println("--- testGenerateContent ---")
    val text = "Write a story about a magic backpack."
    val inputJson =
        generateContentRequest {
            content { part { text { text } } }
            safetySetting {
                category = HarmCategory.HARM_CATEGORY_HARASSMENT
                threshold = Threshold.BLOCK_ONLY_HIGH
            }
        }
    val response = gemini.generateContent(inputJson, model = FLASH_MODEL)
    println(
        response.candidates[0]
            .content.parts
            ?.get(0)!!
            .text!!
            .replace("\n\n", "\n"),
    )

    println("--- testCountTokens ---")
    val inputJson2 =
        CountTokensRequest(
            contents = listOf(Content(parts = listOf(Part(text)))),
        )
    println(gemini.countTokens(inputJson2))

    println("--- testEmbedContent ---")
    val embedRequest =
        EmbedContentRequest(
            content = Content(parts = listOf(Part(text))),
            model = "models/$EMBED_MODEL",
        )
    println(gemini.embedContent(embedRequest, model = EMBED_MODEL))

    println("--- testBatchEmbedContent ---")
    val batchEmbedRequest =
        BatchEmbedRequest(
            requests =
                listOf(
                    EmbedContentRequest(
                        content = Content(parts = listOf(Part(text))),
                        model = "models/$EMBED_MODEL",
                    ),
                ),
        )
    println(gemini.batchEmbedContents(batchEmbedRequest, model = EMBED_MODEL))
}

private suspend fun testModelsAndContent(gemini: Gemini) {
    println("--- testGetModels ---")
    println(gemini.getModels())

    println("--- testGenerateContentWithImage ---")
    val path = Gemini::class.java.getResource("/scones.jpg")
    val image = File(path.toURI())
    val base64Image = Base64.getEncoder().encodeToString(image.readBytes())

    val inputWithImage =
        GenerateContentRequest(
            contents =
                listOf(
                    Content(
                        parts =
                            listOf(
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

    val response = gemini.generateContent(inputWithImage, PRO_MODEL)
    println(
        response.candidates[0]
            .content.parts!!
            .get(0)!!
            .text!!
            .replace("\n\n", "\n"),
    )
}

private suspend fun testCachedContent(gemini: Gemini) {
    println("--- testCachedContent ---")
    val str = "This is a pen".repeat(REPEAT_COUNT)
    val systemInstruction =
        Content(
            parts = listOf(Part(text = "Hello, world!")),
            role = "system",
        )
    val cachedContent =
        CachedContent(
            contents = listOf(Content(parts = listOf(Part(text = str)), role = "user")),
            model = "models/gemini-2.5-flash-lite",
            systemInstruction = systemInstruction,
        )
    val cache = gemini.createCachedContent(cachedContent)
    println(cache)
    println(gemini.listCachedContent())
    println(gemini.getCachedContent(cache.name!!))
    gemini.deleteCachedContent(cache.name!!)
    println("Cached content deleted.")
}

private fun findMoviesFunction(): FunctionDeclaration =
    functionDeclaration {
        name = "find_movies"
        description = "find movie titles currently playing in theaters " +
            "based on any description, genre, title words, etc."
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
        description = "find theaters based on location and optionally movie title " +
            "which is currently playing in theaters"
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

private fun defineFunctionTools(): List<Tool> =
    listOf(
        Tool(
            functionDeclarations =
                listOf(
                    findMoviesFunction(),
                    findTheatersFunction(),
                    getShowtimesFunction(),
                ),
        ),
    )

private suspend fun testFunctionCallingFirstTurn(
    gemini: Gemini,
    tools: List<Tool>,
) {
    println("--- testFunctionCallingFirstTurn ---")
    val exFunction =
        GenerateContentRequest(
            contents =
                listOf(
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

    println(
        gemini
            .generateContent(exFunction, PRO_MODEL)
            .candidates[0]
            .content.parts!!
            .get(0),
    )
}

private suspend fun testFunctionCallingSecondTurn(
    gemini: Gemini,
    tools: List<Tool>,
) {
    println("--- testFunctionCallingSecondTurn ---")
    val content =
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

    val exFunction2 =
        GenerateContentRequest(
            contents =
                listOf(
                    content {
                        role = "user"
                        part { text { "Which theaters in Mountain View show Barbie movie?" } }
                    },
                    content {
                        role = "model"
                        part {
                            functionCall {
                                name = "find_theaters"
                                arg("location", JsonPrimitive("Mountain View, CA"))
                                arg("movie", JsonPrimitive("Barbie"))
                            }
                        }
                    },
                    content {
                        role = "function"
                        part {
                            functionResponse {
                                FunctionResponse(
                                    name = "find_theaters",
                                    response = content,
                                )
                            }
                        }
                    },
                ),
            tools = tools,
        )

    println(
        gemini
            .generateContent(exFunction2, PRO_MODEL)
            .candidates[0]
            .content.parts!!
            .get(0),
    )
}

private fun testPartBuilder() {
    println("--- testPartBuilder ---")
    val examplePart =
        part {
            text { "This is an example text." }
            inlineData {
                mimeType { "text/plain" }
                data { "This is an example inline data." }
            }
        }
    println(examplePart)
}

fun main() =
    runBlocking {
        var apiKey = System.getenv("GEMINI_API_KEY")
        if (apiKey == null) {
            apiKey =
                Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
                    Properties()
                        .apply {
                            load(inputStream)
                        }.getProperty("apiKey")
                }
        }
        if (apiKey.isNullOrEmpty()) {
            println("API key not found. Please set the GEMINI_API_KEY environment variable.")
            return@runBlocking
        }
        val gemini = Gemini(apiKey)
        val tools = defineFunctionTools()

        testContentGeneration(gemini)
        testModelsAndContent(gemini)
//    testCachedContent(gemini)
        testFunctionCallingFirstTurn(gemini, tools)
        testFunctionCallingSecondTurn(gemini, tools)
        testPartBuilder()
    }

class ITTest
