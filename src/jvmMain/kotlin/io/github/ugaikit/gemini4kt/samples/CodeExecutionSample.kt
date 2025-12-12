package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.tool
import kotlinx.coroutines.runBlocking
import java.util.Properties

fun main() {
    runBlocking {
        val path = Gemini::class.java.getResourceAsStream("/prop.properties")
        val prop =
            Properties().also {
                it.load(path)
            }
        val apiKey = prop.getProperty("apiKey")
        val gemini = Gemini(apiKey)
        val text = "What is the sum of the first 50 prime numbers? Generate and run code for the calculation, and make sure you get all 50."
        val inputJson =
            GenerateContentRequest(
                listOf(Content(listOf(Part(text = text)))),
                tools =
                    listOf(
                        tool {
                            codeExecution()
                        },
                    ),
            )
        val response =
            gemini.generateContent(
                inputJson,
                model = "gemini-2.5-flash",
            )

        println(response)
        response.candidates[0].content.parts!!.forEach { part ->
            if (part.text != null) {
                println("Text: ${part.text}")
            }
            if (part.executableCode != null) {
                println("Executable Code (${part.executableCode?.language}):\n${part.executableCode?.code}")
            }
            if (part.codeExecutionResult != null) {
                println("Execution Result (${part.codeExecutionResult?.outcome}):\n${part.codeExecutionResult?.output}")
            }
        }
    }
}

class CodeExecutionSample
