package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.InlineData
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.getImage
import io.github.ugaikit.gemini4kt.tool

/**
 * Demonstrates Gemini 3 Visual Thinking: code execution with image inputs.
 */
object CodeExecutionWithImageSample {
    /**
     * Runs the sample.
     *
     * @param gemini Optional shared client.
     * @param imageProvider Supplies a base64-encoded image; defaults to the bundled sample.
     */
    suspend fun run(
        gemini: Gemini? = null,
        imageProvider: () -> String = { getImage() },
    ) {
        val client = gemini ?: Gemini(getApiKey())
        val base64Image = imageProvider()

        val request =
            GenerateContentRequest(
                contents =
                    arrayOf(
                        Content(
                            arrayOf(
                                Part(
                                    inlineData =
                                        InlineData(
                                            mimeType = "image/jpeg",
                                            data = base64Image,
                                        ),
                                ),
                                Part(text = "Zoom into the expression pedals and tell me how many pedals are there?"),
                            ),
                        ),
                    ),
                tools =
                    arrayOf(
                        tool {
                            codeExecution()
                        },
                    ),
            )

        val response = client.generateContent(request, model = "gemini-3-flash-preview")
        response.candidates.firstOrNull()?.content?.parts?.forEach { part ->
            part.text?.let { println("Text: $it") }
            part.executableCode?.let { executable ->
                println("Executable Code (${executable.language}):\n${executable.code}")
            }
            part.codeExecutionResult?.let { result ->
                println("Execution Result (${result.outcome}):")
                result.output?.let { println(it) }
                result.image?.let { image ->
                    val preview = image.data.take(60)
                    val suffix = if (image.data.length > 60) "..." else ""
                    println("Image Output (${image.mimeType}): $preview$suffix")
                }
            }
        }
    }
}
