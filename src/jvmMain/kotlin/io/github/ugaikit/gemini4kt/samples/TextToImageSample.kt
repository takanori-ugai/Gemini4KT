package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI
import io.github.ugaikit.gemini4kt.GeminiException
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64

private const val IMAGE_MODEL = "gemini-3.1-flash-lite-image"
private const val DEFAULT_OUTPUT_FILE = "gemini-native-image.png"

/**
 * Minimal text-to-image sample for Gemini 3.1 Flash Lite Image.
 */
object TextToImageSample {
    /**
     * Generates an image and writes it to disk.
     *
     * @param client Optional GeminiAI client for tests.
     * @param apiKey Optional API key override.
     * @param prompt Text prompt used to generate the image.
     * @param outputPath Path of the PNG file to write.
     */
    suspend fun run(
        client: GeminiAI? = null,
        apiKey: String? = null,
        prompt: String = "Create a picture of a nano banana dish in a fancy restaurant with a Gemini theme",
        outputPath: String = DEFAULT_OUTPUT_FILE,
    ) {
        val resolvedClient = client ?: GeminiAI(apiKey = apiKey ?: getApiKey())
        try {
            val interaction =
                resolvedClient.createInteraction(
                    CreateInteractionRequest(
                        model = IMAGE_MODEL,
                        input = prompt,
                    ),
                )

            val generatedImage =
                interaction.outputs?.lastOrNull { content ->
                    content.type == "image" && content.data?.isNotBlank() == true
                }

            if (generatedImage?.data == null) {
                println("No generated image was returned.")
                return
            }

            val imageBytes = Base64.getDecoder().decode(generatedImage.data)
            Files.write(Paths.get(outputPath), imageBytes)

            println("Image saved as ${Paths.get(outputPath).toAbsolutePath()}")
            println("Interaction ID: ${interaction.id}")
            println("Model: ${interaction.model ?: IMAGE_MODEL}")
            println("Status: ${interaction.status}")
        } finally {
            if (client == null) {
                resolvedClient.close()
            }
        }
    }

    /**
     * CLI entrypoint used by `./gradlew run`.
     */
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            try {
                run()
            } catch (e: GeminiException) {
                println("Gemini Error running TextToImage sample: ${e.message}")
            } catch (e: IOException) {
                println("IO Error running TextToImage sample: ${e.message}")
            } catch (e: SerializationException) {
                println("Serialization Error running TextToImage sample: ${e.message}")
            }
        }
}
