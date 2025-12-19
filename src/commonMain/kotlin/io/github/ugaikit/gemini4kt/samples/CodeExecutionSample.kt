package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.tool

object CodeExecutionSample {
    suspend fun run(gemini: Gemini? = null) {
        val client = gemini ?: Gemini(getApiKey())
        val text = "What is the sum of the first 50 prime numbers? Generate and run code for the calculation, and make sure you get all 50."
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
        response.candidates.get(0).content.parts?.forEach { part ->
            if (part.text != null) {
                println("Text: ${part.text}")
            }
            if (part.executableCode != null) {
                println("Executable Code (${part.executableCode.language}):\n${part.executableCode.code}")
            }
            if (part.codeExecutionResult != null) {
                println("Execution Result (${part.codeExecutionResult.outcome}):\n${part.codeExecutionResult.output}")
            }
        }
    }
}
