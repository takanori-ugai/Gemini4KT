package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking
import java.io.File

object FileUploadSampleRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            // Use a file from the project's root directory for the sample.
            val imageFile = File("scones.jpg")
            if (!imageFile.exists()) {
                println("Image file not found at path: ${imageFile.absolutePath}")
                return@runBlocking
            }
            FileUploadSample.run(imageFile.path)
        }
    }
}
