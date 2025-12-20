/**
 * Provides platform-specific implementations for the Android platform.
 */
package io.github.ugaikit.gemini4kt

/**
 * Retrieves the API key from the environment variables for the Android platform.
 *
 * @return The API key as a [String].
 */
internal actual fun getApiKey(): String = System.getenv("GEMINI_API_KEY") ?: ""

/**
 * Retrieves an image as a base64 encoded string for the Android platform.
 *
 * Retrieves an image as a base64 encoded string for the Android platform.
 *
 * This function currently returns an empty string as a placeholder.
 *
 * @return An empty string as a placeholder for the image.
 */
internal actual fun getImage(): String = ""
