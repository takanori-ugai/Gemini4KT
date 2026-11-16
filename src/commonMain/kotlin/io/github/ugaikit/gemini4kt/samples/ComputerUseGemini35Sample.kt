package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.GeminiAI

/** Compatibility entry point for the GA Computer Use sample. */
@Deprecated("Use ComputerUseSample with a supported GA model.")
object ComputerUseGemini35Sample {
    /** Requests an action using the GA sample's default model. */
    suspend fun run(gemini: GeminiAI? = null) {
        ComputerUseSample.run(gemini)
    }
}
