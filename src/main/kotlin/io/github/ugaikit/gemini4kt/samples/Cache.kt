package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.CachedContent
import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.HarmCategory
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.SafetySetting
import io.github.ugaikit.gemini4kt.Threshold
import java.util.Properties

fun main() {
    val path = Gemini::class.java.getResourceAsStream("/prop.properties")
    val prop =
        Properties().also {
            it.load(path)
        }
    val apiKey = prop.getProperty("apiKey")
    val gemini = Gemini(apiKey)
    val str = "This is a pen".repeat(10000)
    val cachedContent =
        CachedContent(
            contents = listOf(Content(listOf(Part(text = str)), "user")),
            model = "models/gemini-1.5-flash-002",
            systemInstruction = Content(listOf(Part(text = "Hello, world!")), "system"),
        )
    val cache = gemini.createCachedContent(cachedContent)
    println(cachedContent)
    println(cache)
    println(gemini.listCachedContent())
    println(gemini.getCachedContent(cache.name!!))

    val text = "Summarize the sentences."
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
            cachedContent = cache.name,
        )
    println(
        gemini.generateContent(
            inputJson,
            model = "gemini-1.5-flash-002",
        ).candidates[0].content.parts[0].text!!.replace("\n\n", "\n"),
    )

    println(gemini.deleteCachedContent(cache.name!!))
}

class Cache
