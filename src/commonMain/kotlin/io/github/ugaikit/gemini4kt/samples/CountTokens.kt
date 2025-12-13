package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.*

object CountTokensSample {
    suspend fun run() {
        val apiKey = getApiKey()
        val gemini = Gemini(apiKey)
        val text = "Write a story about a magic backpack."
        val inputJson = CountTokensRequest(listOf(Content(listOf(Part(text)))))
        println(gemini.countTokens(inputJson))
    }
}
