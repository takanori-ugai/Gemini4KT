package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import kotlinx.coroutines.runBlocking
import java.util.Properties

/**
 * The entry point of the application.
 *
 * This function loads the API key from a properties file and initializes the
 * Gemini client with it. It then retrieves and prints the list of models
 * available through the Gemini client.
 */
object ModelsSample {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        // Load the API key from the properties file
        val apiKey =
        Gemini::class.java.getResourceAsStream("/prop.properties").use { inputStream ->
            Properties()
                .apply {
                    load(inputStream)
                }.getProperty("apiKey")
        }

    // Initialize the Gemini client with the API key
    val gemini = Gemini(apiKey)

    // Retrieve and print each model
    gemini.getModels().models.forEach(::println)
    }
}

/**
 * Represents a collection of models.
 *
 * This class is a placeholder for the actual implementation that would
 * manage or interact with a set of models in the context of the application.
 */
class Models
