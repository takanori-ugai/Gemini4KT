package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.InlineData
import io.github.ugaikit.gemini4kt.Part
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Base64
import java.util.Properties

class InputWithImage {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            val path = Gemini::class.java.getResourceAsStream("/prop.properties")
            val prop =
                Properties().also {
                    it.load(path)
                }
            val apiKey = prop.getProperty("apiKey")
            val gemini = Gemini(apiKey)
            val image = File(Gemini::class.java.getResource("/scones.jpg").toURI())
            val base64Image = Base64.getEncoder().encodeToString(image.readBytes())

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
}
