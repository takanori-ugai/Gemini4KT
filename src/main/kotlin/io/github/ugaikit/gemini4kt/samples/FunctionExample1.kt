package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.FunctionDeclaration
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.Schema
import io.github.ugaikit.gemini4kt.Tool
import java.util.Properties

fun main() {
    val path = Gemini::class.java.getResourceAsStream("/prop.properties")
    val prop = Properties().also { it.load(path) }
    val apiKey = prop.getProperty("apiKey")
    val gemini = Gemini(apiKey)

    val findMoviesFunction =
        FunctionDeclaration(
            name = "find_movies",
            description = "find movie titles currently playing in theaters based on any description, genre, title words, etc.",
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
                            "description" to
                                Schema(
                                    type = "string",
                                    description = "Any kind of description including category or genre",
                                ),
                        ),
                    required = listOf("description"),
                ),
        )

    val findTheatersFunction =
        FunctionDeclaration(
            name = "find_theaters",
            description = "find theaters based on location and optionally movie title which is currently playing in theaters",
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
                        ),
                    required = listOf("location"),
                ),
        )

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
                    Content(
                        role = "user",
                        parts =
                            listOf(
                                Part(text = "Which theaters in Mountain View show Barbie movie?"),
                            ),
                    ),
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
        gemini.generateContent(
            exFunction,
            "gemini-2.0-flash-exp",
        ).candidates[0].content.parts[0],
    )
}

class FunctionExample1
