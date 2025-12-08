package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.util.Properties

fun main() =
    runBlocking {
        val path = Gemini::class.java.getResourceAsStream("/prop.properties")
        val prop =
            Properties().also {
                if (path != null) it.load(path)
            }
        val apiKey = System.getenv("GEMINI_API_KEY") ?: prop.getProperty("apiKey")
        val gemini = Gemini(apiKey)
        val text = "Write a story about a magic backpack."
        val inputJson =
            GenerateContentRequest(
                listOf(Content(listOf(Part(text)))),
            )
        val flow =
            gemini.streamGenerateContent(
                inputJson,
                model = "gemini-1.5-flash",
            )
        flow.collect { response ->
            response.candidates.forEach { candidate ->
                candidate.content.parts?.forEach { part ->
                    print(part.text)
                }
            }
        }
    }
