package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.InlineData
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.getImage

object InputWithImage {
    suspend fun run(args: Array<String>) {
        val base64Image = getImage()
        val gemini = Gemini(getApiKey())

        val inputWithImage =
            GenerateContentRequest(
                listOf(
                    Content(
                        listOf(
                            Part(text = "What is this picture?"),
                            Part(
                                inlineData =
                                    InlineData(
                                        mimeType = "image/jpeg",
                                        data = base64Image,
                                    ),
                            ),
                        ),
                    ),
                ),
            )
        println(
            gemini
                .generateContent(
                    inputWithImage,
                    "gemini-2.5-flash-lite",
                ).candidates[0]
                .content.parts!!
                .get(0)
                .text!!
                .replace("\n\n", "\n"),
        )
    }
}
