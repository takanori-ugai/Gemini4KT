package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.tool

object GoogleSearchSample {
    suspend fun run(gemini: Gemini? = null) {
        val client = gemini ?: Gemini(getApiKey())
        val text = "Who won the euro 2024?"
        val inputJson =
            GenerateContentRequest(
                arrayOf(Content(arrayOf(Part(text)))),
                tools =
                    arrayOf(
                        tool {
                            googleSearch()
                        },
                    ),
            )
        val response =
            client.generateContent(
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
