package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.CountTokensRequest
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.Part
import kotlinx.coroutines.runBlocking
import java.util.Properties

object CountTokensSample {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val path = Gemini::class.java.getResourceAsStream("/prop.properties")
        val prop =
        Properties().also {
            it.load(path)
        }
    val apiKey = prop.getProperty("apiKey")
    val gemini = Gemini(apiKey)
    val text = "Write a story about a magic backpack."
    val inputJson = CountTokensRequest(listOf(Content(listOf(Part(text)))))
    println(gemini.countTokens(inputJson))
    }
}

class CountTokens
