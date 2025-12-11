package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.BatchEmbedRequest
import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.EmbedContentRequest
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.Part
import kotlinx.coroutines.runBlocking
import java.util.Properties

object EmbedContentSample {
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

class EmbedContent
