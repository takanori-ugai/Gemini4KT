package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.CachedContent
import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.HarmCategory
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.SafetySetting
import io.github.ugaikit.gemini4kt.Threshold
import kotlinx.coroutines.runBlocking
import java.util.Properties

private const val REPEAT_COUNT = 10000

object CacheSample {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            val path = Gemini::class.java.getResourceAsStream("/prop.properties")
            val prop =
                Properties().also {
                    it.load(path)
                }
            val apiKey = prop.getProperty("apiKey")
            val gemini = Gemini(apiKey)
            val str = "This is a pen".repeat(REPEAT_COUNT)
            val cachedContent =
                CachedContent(
                    contents = listOf(Content(listOf(Part(text = str)), "user")),
                    model = "models/gemini-2.5-flash-lite",
                    systemInstruction = Content(listOf(Part(text = "Hello, world!")), "system"),
                )
            val cache = gemini.createCachedContent(cachedContent)
            println(cachedContent)
            println(cache)
            println(gemini.listCachedContent())
            println(gemini.getCachedContent(cache.name!!))
            println("--------------------------------------------------------------")

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
                gemini
                    .generateContent(
                        inputJson,
                        model = "gemini-2.5-flash-lite",
                    ).candidates[0]
                    .content.parts!!
                    .get(0)
                    .text!!
                    .replace("\n\n", "\n"),
            )

            gemini.deleteCachedContent(cache.name!!)
        }
}

class Cache
