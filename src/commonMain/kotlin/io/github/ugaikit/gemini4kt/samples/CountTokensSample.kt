package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.CountTokensRequest
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.Part

/**
 * Represents the count tokens sample.
 */
object CountTokensSample {
    /**
     * Handles run.
     *
     * @param gemini The gemini.
     */
    suspend fun run(gemini: Gemini? = null) {
        withGeminiClient(gemini) { client ->
            val text = "Write a story about a magic backpack."
            val inputJson = CountTokensRequest(listOf(Content(arrayOf(Part(text)))))
            println(client.countTokens(inputJson))
        }
    }
}
