package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.getApiKey

/**
 * Represents a collection of models.
 *
 * This class is a placeholder for the actual implementation that would
 * manage or interact with a set of models in the context of the application.
 */
object Models {
    suspend fun listModels() {
        // Initialize the Gemini client with the API key
        val gemini = Gemini(getApiKey())

        // Retrieve and print each model
        gemini.getModels().models.forEach {
            println(it)
        }
    }
}
