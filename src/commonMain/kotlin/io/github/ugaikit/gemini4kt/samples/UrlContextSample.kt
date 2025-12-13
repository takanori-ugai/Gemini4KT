package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Mode
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.functionCallingConfig
import io.github.ugaikit.gemini4kt.tool
import io.github.ugaikit.gemini4kt.toolConfig

object UrlContextSample {
    suspend fun run(gemini: Gemini) {
        val tools =
            tool {
                urlContext()
            }

        val toolConfig =
            toolConfig {
                functionCallingConfig {
                    mode = Mode.ANY
                    allowFunction("url_context")
                }
            }

        val input =
            GenerateContentRequest(
                contents =
                    listOf(
                        Content(
                            parts =
                                listOf(
                                    Part(text = "Extract the content of the following URL: https://www.google.com"),
                                ),
                        ),
                    ),
                tools = listOf(tools),
                // toolConfig = toolConfig
            )

        val response =
            gemini.generateContent(
                input,
            )
        println(
            response.candidates[0]
                .content.parts!![0]
                .text,
        )
        println(
            response.candidates[0]
                .urlContextMetadata!!
                .urlMetadata[0]
                .retrievedUrl,
        )
    }
}
