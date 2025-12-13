package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

object StreamGenerateContentSample {
    suspend fun run(gemini: Gemini) {
        val text = "Tell me a story about a magic backpack."

        val input =
            GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = text)))),
            )

        gemini
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
