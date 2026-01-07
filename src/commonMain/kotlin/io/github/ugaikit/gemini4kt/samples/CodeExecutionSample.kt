package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.tool

/**
 * Represents the code execution sample.
 */
object CodeExecutionSample {
    /**
     * Handles run.
     *
     * @param gemini The gemini.
     */
    suspend fun run(gemini: Gemini? = null) {
        val client = gemini ?: Gemini(getApiKey())
        val text =
            "What is the sum of the first 50 prime numbers? " +
                "Generate and run code for the calculation, and make sure you get all 50."
        val inputJson =
            GenerateContentRequest(
                arrayOf(Content(arrayOf(Part(text = text)))),
                tools =
                    arrayOf(
                        tool {
                            codeExecution()
                        },
                    ),
            )
        val response =
            client.generateContent(
                inputJson,
                model = "gemini-2.5-flash",
            )

        println(response)
        printExecutionParts(response.candidates[0].content.parts)
    }
}
