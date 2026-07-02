package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini

/**
 * Represents a collection of models.
 *
 * This class is a placeholder for the actual implementation that would
 * manage or interact with a set of models in the context of the application.
 */
object Models {
    /**
     * Handles list models.
     *
     * @param gemini The gemini.
     */
    suspend fun listModels(gemini: Gemini? = null) {
        withGeminiClient(gemini) { client ->
            client.getModels().models.forEach(::println)
        }
    }
}
