package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.BatchEmbedRequest
import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.EmbedContentRequest
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.getApiKey

object EmbedContent {
    suspend fun run() {
        val gemini = Gemini(getApiKey())
        val text = "Write a story about a magic backpack."
        val embedRequest =
            EmbedContentRequest(
                content = Content(listOf(Part(text))),
                model = "models/text-embedding-004",
            )
        println(gemini.embedContent(embedRequest, model = "text-embedding-004"))
        val batchEmbedRequest =
            BatchEmbedRequest(
                listOf(
                    EmbedContentRequest(
                        content = Content(listOf(Part(text))),
                        model = "models/text-embedding-004",
                    ),
                ),
            )
        println(gemini.batchEmbedContents(batchEmbedRequest, model = "text-embedding-004"))
    }
}
