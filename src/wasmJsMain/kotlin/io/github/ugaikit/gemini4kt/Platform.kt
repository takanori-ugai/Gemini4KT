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
    // Environment variables are not directly accessible in Wasm/JS in the same way.
    // For now, we can return an empty string or specific value.
    // In a real browser app, this might come from configuration or user input.
    return ""
}

/**
 * Retrieves an image as a base64 encoded string for the Wasm/JS platform.
 *
 * This function currently returns an empty string as a placeholder.
 *
 * @return An empty string as a placeholder.
 */
internal actual fun getImage(): String = ""
