/**
 * Provides platform-specific implementations for the Wasm/JS platform.
 */
package io.github.ugaikit.gemini4kt

/**
 * Retrieves the API key for the Wasm/JS platform.
 *
 * Environment variables are not directly accessible in Wasm/JS.
 * This function returns an empty string or a specific value.
 *
 * @return An empty string as a placeholder for the image.
 */
internal actual fun getApiKey(): String {
    throw IllegalStateException(
        "GEMINI_API_KEY is not available in Wasm/JS. Pass apiKey explicitly when constructing the client.",
    )
}

/**
 * Retrieves an image as a base64 encoded string for the Wasm/JS platform.
 *
 * This function currently returns an empty string as a placeholder.
 *
 * @return An empty string as a placeholder.
 */
internal actual fun getImage(): String = ""
