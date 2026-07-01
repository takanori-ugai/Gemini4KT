package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GeminiFunction
import io.github.ugaikit.gemini4kt.GeminiParameter
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.generateContent
import io.github.ugaikit.gemini4kt.getApiKey

/**
 * Direct Kotlin function binding sample for automatic function calling.
 */
@GeminiFunction(description = "Powers the spinning disco ball on or off.")
private fun powerDiscoBall(
    @GeminiParameter(description = "Whether to turn the disco ball on or off.") power: Boolean,
): Map<String, Any> = mapOf("status" to "Disco ball powered ${if (power) "on" else "off"}")

@GeminiFunction(description = "Play music that matches the party mood.")
private fun startMusic(
    @GeminiParameter(description = "Whether the music should be energetic.") energetic: Boolean,
    @GeminiParameter(description = "Whether the music should be loud.") loud: Boolean,
): Map<String, Any> =
    mapOf(
        "music_type" to if (energetic) "energetic" else "chill",
        "volume" to if (loud) "loud" else "quiet",
    )

@GeminiFunction(description = "Set room brightness from 0.0 (off) to 1.0 (full).")
private fun dimLights(
    @GeminiParameter(description = "Target brightness from 0.0 to 1.0.") brightness: Double,
): Map<String, Any> = mapOf("brightness" to brightness)

object FunctionExample4 {
    /**
     * Runs the disco-ball scenario with direct function references.
     */
    suspend fun run(gemini: Gemini? = null) {
        val client = gemini ?: Gemini(getApiKey())
        val request =
            GenerateContentRequest(
                contents =
                    arrayOf(
                        Content(
                            role = "user",
                            parts =
                                arrayOf(
                                    Part(text = "Do everything you need to turn this place into a party!"),
                                ),
                        ),
                    ),
            )

        val response =
            client.generateContent(
                request,
                ::powerDiscoBall,
                ::startMusic,
                ::dimLights,
                model = "gemma-4-31b-it",
                maxIterations = 8,
            )

        val firstCandidate = response.candidates.firstOrNull()
        val finalText = response.getText()
        if (finalText != null) {
            println("Final response: $finalText")
        } else {
            println("Final candidate content: ${firstCandidate?.content}")
        }
    }
}
