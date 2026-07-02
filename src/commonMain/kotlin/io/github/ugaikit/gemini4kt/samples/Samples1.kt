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

/**
 * Represents the samples1.
 */
object Samples1 {
    /**
     * Handles run.
     *
     * @param gemini The gemini.
     */
    suspend fun run(gemini: Gemini? = null) {
        withGeminiClient(gemini) { client ->
            val text = "Write a story about a magic backpack."
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
//            systemInstruction = Content(listOf(Part("You are a excellent assistant"))),
                    generationConfig =
                        GenerationConfig(
                            thinkingConfig = ThinkingConfig(-1),
                        ),
                )
            println(
                client
                    .generateContent(
                        inputJson,
                        model = "gemma-4-31b-it",
                    ).candidates[0]
                    .content.parts!!
                    .get(0)
                    .text!!
                    .replace("\n\n", "\n"),
            )
        }
    }
}
