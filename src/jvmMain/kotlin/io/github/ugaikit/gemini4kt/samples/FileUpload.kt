package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.FileData
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.util.Properties

fun main() {
    val path = Gemini::class.java.getResourceAsStream("/prop.properties")
    val prop =
        Properties().also {
            it.load(path)
        }
    val apiKey = prop.getProperty("apiKey")
    if (apiKey.isNullOrEmpty()) {
        println("API key not found. Please set the GEMINI_API_KEY environment variable.")
        return
    }
    val gemini = Gemini(apiKey = apiKey)

    runBlocking {
        // Use a file from the project's root directory for the sample.
        val imageFile = File("scones.jpg")
        if (!imageFile.exists()) {
            println("Image file not found at path: ${imageFile.absolutePath}")
            return@runBlocking
        }

        println("Uploading file...")
        val uploadedFile =
            gemini.uploadFile(
                file = imageFile,
                mimeType = "image/jpeg",
                displayName = "Scones",
            )
        println("File uploaded successfully. URI: ${uploadedFile.uri}")

        val request =
            GenerateContentRequest(
                contents =
                    listOf(
                        Content(
                            parts =
                                listOf(
                                    Part(text = "What is in this image?"),
                                    Part(
                                        fileData =
                                            FileData(
                                                mimeType = uploadedFile.mimeType,
                                                fileUri = uploadedFile.uri,
                                            ),
                                    ),
                                ),
                        ),
                    ),
            )

        println("Generating content from file...")
        try {
            val response = gemini.generateContent(request, model = "gemini-2.5-flash-lite")
            response.candidates.forEach { candidate ->
                candidate.content.parts!!.forEach { part ->
                    println(part.text)
                }
            }
        } catch (e: IOException) {
            println("An error occurred: ${e.message}")
            e.printStackTrace()
        }
    }
}
