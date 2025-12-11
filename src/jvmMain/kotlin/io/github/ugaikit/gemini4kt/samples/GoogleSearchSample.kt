package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.tool
import kotlinx.coroutines.runBlocking
import java.util.Properties

class GoogleSearchSample {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            val path = Gemini::class.java.getResourceAsStream("/prop.properties")
            val prop =
                Properties().also {
                    it.load(path)
                }
            val apiKey = prop.getProperty("apiKey")
            val gemini = Gemini(apiKey)
            val text = "Who won the euro 2024?"
            val inputJson =
                GenerateContentRequest(
                    listOf(Content(listOf(Part(text)))),
                    tools =
                        listOf(
                            tool {
                                googleSearch()
                            },
                        ),
                )
            val response =
                gemini.generateContent(
                    inputJson,
                    model = "gemini-2.0-flash-exp",
                )
            println(
                response.candidates[0]
                    .content.parts!!
                    .get(0)
                    .text,
            )

            // Check if grounding metadata is present (optional, but good for verification)
            val groundingMetadata = response.candidates[0].groundingMetadata
            if (groundingMetadata != null) {
                println("Grounding Metadata found: $groundingMetadata")
            }
        }
    }
}
