package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.GenerationConfig
import io.github.ugaikit.gemini4kt.HarmCategory
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.SafetySetting
import io.github.ugaikit.gemini4kt.ThinkingConfig
import io.github.ugaikit.gemini4kt.Threshold
import kotlinx.coroutines.runBlocking
import java.util.Properties

object Samples1Sample {
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
//            systemInstruction = Content(listOf(Part("You are a excellent assistant"))),
                    generationConfig =
                        GenerationConfig(
                            thinkingConfig = ThinkingConfig(-1),
                        ),
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
        }
}

class Samples1
