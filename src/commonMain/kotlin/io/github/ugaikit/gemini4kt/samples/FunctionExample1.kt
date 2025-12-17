package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.FunctionDeclaration
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.Schema
import io.github.ugaikit.gemini4kt.Tool
import io.github.ugaikit.gemini4kt.getApiKey

object FunctionExample1 {
    suspend fun run(gemini: Gemini? = null) {
        val client = gemini ?: Gemini(getApiKey())

        val exFunction =
            GenerateContentRequest(
                contents =
                    arrayOf(
                        Content(
                            role = "user",
                            parts =
                                arrayOf(
                                    Part(text = "Which theaters in Mountain View show Barbie movie?"),
                                ),
                        ),
                    ),
                tools =
                    arrayOf(
                        Tool(
                            functionDeclarations = getFunctionDeclarations(),
                        ),
                    ),
            )

        println(
            client
                .generateContent(
                    exFunction,
                    "gemini-2.5-flash-lite",
                ).candidates[0]
                .content.parts!!
                .get(0),
        )
    }

    private fun findMoviesFunction(): FunctionDeclaration =
        FunctionDeclaration(
            name = "find_movies",
            description =
                "find movie titles currently playing in theaters " +
                    "based on any description, genre, title words, etc.",
            parameters =
                Schema(
                    type = "object",
                    properties =
                        mapOf(
                            "location" to
                                Schema(
                                    type = "string",
                                    description =
                                        "The city and state, e.g. San Francisco, CA " +
                                            "or a zip code e.g. 95616",
                                ),
                            "description" to
                                Schema(
                                    type = "string",
                                    description = "Any kind of description including category or genre",
                                ),
                        ),
                    required = arrayOf("description"),
                ),
        )

    private fun findTheatersFunction(): FunctionDeclaration =
        FunctionDeclaration(
            name = "find_theaters",
            description =
                "find theaters based on location and optionally movie title " +
                    "which is currently playing in theaters",
            parameters =
                Schema(
                    type = "object",
                    properties =
                        mapOf(
                            "location" to
                                Schema(
                                    type = "string",
                                    description =
                                        "The city and state, e.g. San Francisco, CA " +
                                            "or a zip code e.g. 95616",
                                ),
                            "movie" to
                                Schema(
                                    type = "string",
                                    description = "Any movie title",
                                ),
                        ),
                    required = arrayOf("location"),
                ),
        )

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
                                    description =
                                        "The city and state, e.g. San Francisco, CA " +
                                            "or a zip code e.g. 95616",
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
                    required = arrayOf("location", "movie", "theater", "date"),
                ),
        )

    private fun getFunctionDeclarations(): Array<FunctionDeclaration> = arrayOf(findMoviesFunction(), findTheatersFunction(), getShowtimesFunction())
}
