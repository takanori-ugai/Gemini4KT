package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.InlineData
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.getImage

/**
 * Represents the input with image.
 */
object InputWithImage {
    /**
     * Handles run.
     *
     * @param args The args.
     * @param gemini The gemini.
     * @param imageProvider The image provider.
     */
    suspend fun run(
        args: Array<String>,
        gemini: Gemini? = null,
        imageProvider: () -> String = { getImage() },
    ) {
        val client = gemini ?: Gemini(getApiKey())
        val base64Image = imageProvider()

        val inputWithImage =
            GenerateContentRequest(
                arrayOf(
                    Content(
                        arrayOf(
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
            client
                .generateContent(
                    inputWithImage,
                    "gemma-4-31b-it",
                ).candidates[0]
                .content.parts!!
                .get(0)
                .text!!
                .replace("\n\n", "\n"),
        )
    }
}
