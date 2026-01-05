/**
 * Provides platform-specific implementations for the JS platform.
 */
package io.github.ugaikit.gemini4kt

/**
 * Retrieves the API key for the JS platform.
 *
 * Environment variables are not directly accessible in JS.
 * This function returns an empty string or a specific value.
 *
 * @return An empty string as a placeholder for the image.
 */
internal actual fun getApiKey(): String {
    // Environment variables are not directly accessible in JS in the same way.
    // In a Node.js environment, we could use process.env, but for now returning empty string
    // to match WasmJs implementation or simple default.
    return js("process.env[\"GEMINI_API_KEY\"]")
}

/**
 * Retrieves an image as a base64 encoded string for the JS platform.
 *
 * This function currently returns an empty string as a placeholder.
 *
 * @return An empty string as a placeholder.
 */
internal actual fun getImage(): String = ""
