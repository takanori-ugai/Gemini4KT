package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.BatchEmbedRequest
import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.EmbedContentRequest
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.HarmCategory
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.SafetySetting
import io.github.ugaikit.gemini4kt.Threshold
import io.github.ugaikit.gemini4kt.embedModel
import java.util.Properties

fun main() {
    val path = Gemini::class.java.getResourceAsStream("/prop.properties")
    val prop =
        Properties().also {
            it.load(path)
        }
    val apiKey = prop.getProperty("apiKey")
    val gemini = Gemini(apiKey)
    val text = "Write a story about a magic backpack."
    val inputJson =
        GenerateContentRequest(
            listOf(Content(listOf(Part(text)))),
            safetySettings =
                listOf(
                    SafetySetting(
                        category = HarmCategory.HARM_CATEGORY_HARASSMENT,
                        threshold = Threshold.BLOCK_ONLY_HIGH,
                    ),
                ),
        )
    val embedRequest =
        EmbedContentRequest(
            content = Content(listOf(Part(text))),
            model = "models/text-embedding-004",
        )
    println(gemini.embedContent(embedRequest, model = embedModel))
    val batchEmbedRequest =
        BatchEmbedRequest(
            listOf(
                EmbedContentRequest(
                    content = Content(listOf(Part(text))),
                    model = "models/text-embedding-004",
                ),
            ),
        )
    println(gemini.batchEmbedContents(batchEmbedRequest, model = embedModel))
}

class EmboedCOntent
