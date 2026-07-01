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
    val nodeApiKey =
        js("(typeof process !== 'undefined' && process.env && process.env['GEMINI_API_KEY']) ? process.env['GEMINI_API_KEY'] : null") as String?
    return requireNonBlankCredential(
        nodeApiKey,
        "GEMINI_API_KEY environment variable on JS",
    )
}

/**
 * Retrieves an image as a base64 encoded string for the JS platform.
 */
internal actual fun getImage(): String = throw UnsupportedOperationException("getImage() is not supported on JS. Provide image data explicitly.")
