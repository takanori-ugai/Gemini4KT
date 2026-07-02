package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.BatchEmbedRequest
import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.EmbedContentRequest
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.Part

/**
 * Represents the embed content.
 */
object EmbedContent {
    /**
     * Handles run.
     *
     * @param gemini The gemini.
     */
    suspend fun run(gemini: Gemini? = null) {
        withGeminiClient(gemini) { client ->
            val text = "Write a story about a magic backpack."
            val embedRequest =
                EmbedContentRequest(
                    content = Content(arrayOf(Part(text))),
                    model = "models/gemini-embedding-001",
                )
            println(client.embedContent(embedRequest, model = "gemini-embedding-001"))
            val batchEmbedRequest =
                BatchEmbedRequest(
                    listOf(
                        EmbedContentRequest(
                            content = Content(arrayOf(Part(text))),
                            model = "models/gemini-embedding-001",
                        ),
                    ),
                )
            println(client.batchEmbedContents(batchEmbedRequest, model = "gemini-embedding-001"))
        }
    }
}
