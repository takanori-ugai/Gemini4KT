package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GeminiException
import io.github.ugaikit.gemini4kt.Modality
import io.github.ugaikit.gemini4kt.generateContentRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64
import java.util.Properties

class TextToImage(
    private val gemini: Gemini,
) {
    suspend fun generateImage() {
        val prompt = "Create a picture of a nano banana dish in a fancy restaurant with a Gemini theme"

        val request =
            generateContentRequest {
                content {
                    part {
                        text { prompt }
                    }
                }
                generationConfig {
                    responseModality(Modality.TEXT)
                    responseModality(Modality.IMAGE)
                    imageConfig {
                        aspectRatio { "16:9" }
                    }
                }
            }

        // Note: The model name "gemini-2.5-flash-image" is used in the example.
        // Ensure this model is available to your API key.
        val response = gemini.generateContent(request, "gemini-2.0-flash-exp-image-generation")

        for (candidate in response.candidates) {
            for (part in candidate.content.parts!!) {
                if (part.text != null) {
                    println("Text response: ${part.text}")
                }
                if (part.inlineData != null) {
                    val imageData = part.inlineData.data
                    // Decode Base64 string to byte array
                    val imageBytes = Base64.getDecoder().decode(imageData)
                    val filename = "gemini-native-image.png"
                    Files.write(Paths.get(filename), imageBytes)
                    println("Image saved as $filename")
                }
            }
        }
    }
}

object TextToImageSample {
    @JvmStatic
    fun main(args: Array<String>) =
    runBlocking {
        val apiKey =
            Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
                Properties()
                    .apply {
                        load(inputStream)
                    }.getProperty("apiKey")
            }
        val gemini = Gemini(apiKey)
        val sample = TextToImage(gemini)
        try {
            sample.generateImage()
        } catch (e: GeminiException) {
            println("Gemini Error running TextToImage sample: ${e.message}")
        } catch (e: IOException) {
            println("IO Error running TextToImage sample: ${e.message}")
        } catch (e: SerializationException) {
            println("Serialization Error running TextToImage sample: ${e.message}")
        }
    }
}
