/**
 * Provides platform-specific implementations for the native platform.
 */
package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.Json
import platform.posix.getenv

/**
 * Creates an HTTP client configured for the native platform.
 *
 * @param json The JSON configuration for serialization.
 * @return A configured [HttpClient] instance.
 */
actual fun createHttpClient(json: Json): HttpClient =
    HttpClient {
        configureGeminiHttpClient(json, installLogging = true)
    }

@OptIn(ExperimentalForeignApi::class)
/**
 * Retrieves the API key from the environment variables for the native platform.
 *
 * @return The API key as a [String].
 */
internal actual fun getApiKey(): String =
    requireNonBlankCredential(
        getenv("GEMINI_API_KEY")?.toKString(),
        "GEMINI_API_KEY environment variable on native",
    )

/**
 * Retrieves an image as a base64 encoded string for the native platform.
 */
internal actual fun getImage(): String = throw UnsupportedOperationException("getImage() is not supported on native targets. Provide image data explicitly.")
