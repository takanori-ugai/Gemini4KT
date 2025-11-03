package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.io.File
import java.util.Base64

val embedModel = "text-embedding-004"

fun main() {
    val apiKey = System.getenv("GEMINI_API_KEY")
    val gemini = Gemini(apiKey)
    val text = "Write a story about a magic backpack."

    val inputJson =
        generateContentRequest {
            content { part { text { text } } }
            safetySetting {
                category = HarmCategory.HARM_CATEGORY_HARASSMENT
                threshold = Threshold.BLOCK_ONLY_HIGH
            }
        }
    println(
        gemini
            .generateContent(
                inputJson,
                model = "gemini-2.0-flash-exp",
            ).candidates[0]
            .content.parts[0]
            .text!!
            .replace("\n\n", "\n"),
    )

    val inputJson2 =
        CountTokensRequest(
            contents = listOf(Content(parts = listOf(Part(text)))),
        )
    println(gemini.countTokens(inputJson2))

    val embedRequest =
        EmbedContentRequest(
            content = Content(parts = listOf(Part(text))),
            model = "models/$embedModel",
        )
    println(gemini.embedContent(embedRequest, model = embedModel))

    val batchEmbedRequest =
        BatchEmbedRequest(
            requests =
                listOf(
                    EmbedContentRequest(
                        content = Content(parts = listOf(Part(text))),
                        model = "models/text-embedding-004",
                    ),
                ),
        )
    println(gemini.batchEmbedContents(batchEmbedRequest, model = embedModel))

    println(gemini.getModels())

    val path = Gemini::class.java.getResource("/scones.jpg")
    val imagePath = "scones.jpg"
    val imageFile = File(imagePath)
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

    println(
        gemini
            .generateContent(
                inputWithImage,
                "gemini-2.0-flash-exp",
            ).candidates[0]
            .content.parts[0]
            .text!!
            .replace("\n\n", "\n"),
    )

    val str = "This is a pen".repeat(10000)
    val cachedContent =
        CachedContent(
            contents = listOf(Content(parts = listOf(Part(text = str)), role = "user")),
            model = "models/gemini-1.5-flash-002",
            systemInstruction = Content(parts = listOf(Part(text = "Hello, world!")), role = "system"),
        )
    val cache = gemini.createCachedContent(cachedContent)
    println(cache)
    println(gemini.listCachedContent())
    println(gemini.getCachedContent(cache.name!!))
    println(gemini.deleteCachedContent(cache.name!!))

    val findMoviesFunction =
        functionDeclaration {
            name = "find_movies"
            description = "find movie titles currently playing in theaters based on any description, genre, title words, etc."
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

    val findTheatersFunction =
        functionDeclaration {
            name = "find_theaters"
            description = "find theaters based on location and optionally movie title which is currently playing in theaters"
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

    val getShowtimesFunction =
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
            tools =
                listOf(
                    Tool(
                        functionDeclarations =
                            listOf(
                                findMoviesFunction,
                                findTheatersFunction,
                                getShowtimesFunction,
                            ),
                    ),
                ),
        )

    println(
        gemini
            .generateContent(
                exFunction,
                "gemini-2.0-flash-exp",
            ).candidates[0]
            .content.parts[0],
    )

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
            tools =
                listOf(
                    Tool(
                        functionDeclarations =
                            listOf(
                                findMoviesFunction,
                                findTheatersFunction,
                                getShowtimesFunction,
                            ),
                    ),
                ),
        )

    val examplePart =
        part {
            text { "This is an example text." }
            inlineData {
                mimeType { "text/plain" }
                data { "This is an example inline data." }
            }
        }
    println(examplePart)

    println(
        gemini
            .generateContent(
                exFunction2,
                "gemini-2.0-flash-exp",
            ).candidates[0]
            .content.parts[0],
    )
}

class ITTest
