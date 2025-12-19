package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.FileData
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.getApiKey
import kotlinx.io.files.Path

object FileUploadSample {
    suspend fun run(
        imagePath: String,
        gemini: Gemini? = null,
    ) {
        val client = gemini ?: Gemini(getApiKey())

        println("Uploading file...")
        val uploadedFile =
            client.uploadFile(
                file = Path(imagePath),
                mimeType = "image/jpeg",
                displayName = "Scones",
            )
        println("File uploaded successfully. URI: ${uploadedFile.uri}")

        val request =
            GenerateContentRequest(
                contents =
                    arrayOf(
                        Content(
                            parts =
                                arrayOf(
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
            val response = client.generateContent(request, model = "gemini-2.5-flash-lite")
            response.candidates.forEach { candidate ->
                candidate.content.parts?.forEach { part ->
                    println(part.text)
                }
            }
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
            // e.printStackTrace() is not available in common code standard library, but we can print exception
            println(e)
        }
    }
}
