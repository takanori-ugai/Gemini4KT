package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.CachedContent
import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.HarmCategory
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.SafetySetting
import io.github.ugaikit.gemini4kt.Threshold
import io.github.ugaikit.gemini4kt.getApiKey

private const val REPEAT_COUNT = 10000

object Cache {
    suspend fun run(gemini: Gemini? = null) {
        val client = gemini ?: Gemini(getApiKey())
        val str = "This is a pen".repeat(REPEAT_COUNT)
        val cachedContent =
            CachedContent(
                contents = arrayOf(Content(arrayOf(Part(text = str)), "user")),
                model = "models/gemini-2.5-flash-lite",
                systemInstruction = Content(arrayOf(Part(text = "Hello, world!")), "system"),
            )
        val cache = client.createCachedContent(cachedContent)
        println(cachedContent)
        println(cache)
        println(client.listCachedContent())
        println(client.getCachedContent(cache.name!!))
        println("--------------------------------------------------------------")

        val text = "Summarize the sentences."
        val inputJson =
            GenerateContentRequest(
                arrayOf(Content(arrayOf(Part(text)))),
                safetySettings =
                    arrayOf(
                        SafetySetting(
                            category = HarmCategory.HARM_CATEGORY_HARASSMENT,
                            threshold = Threshold.BLOCK_ONLY_HIGH,
                        ),
                    ),
                cachedContent = cache.name,
            )
        println(
            client
                .generateContent(
                    inputJson,
                    model = "gemini-2.5-flash-lite",
                ).candidates[0]
                .content.parts!!
                .get(0)
                .text!!
                .replace("\n\n", "\n"),
        )

        client.deleteCachedContent(cache.name!!)
    }
}
