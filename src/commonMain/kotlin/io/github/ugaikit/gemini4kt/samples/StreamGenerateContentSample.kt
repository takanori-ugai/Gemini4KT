package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.getApiKey
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * Represents the stream generate content sample.
 */
object StreamGenerateContentSample {
    /**
     * Handles run.
     *
     * @param gemini The gemini.
     */
    suspend fun run(gemini: Gemini? = null) {
        val client = gemini ?: Gemini(getApiKey())
        val text = "Tell me a story about a magic backpack."

        val input =
            GenerateContentRequest(
                contents = arrayOf(Content(parts = arrayOf(Part(text = text)))),
            )

        client
            .streamGenerateContent(input)
            .onEach { response ->
                print(
                    response.candidates[0]
                        .content.parts
                        ?.get(0)
                        ?.text ?: "",
                )
            }.collect()
    }
}
