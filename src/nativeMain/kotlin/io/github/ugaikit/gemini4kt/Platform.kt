/**
 * Provides platform-specific implementations for the native platform.
 */
package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
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
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.NONE
        }
    }

@OptIn(ExperimentalForeignApi::class)
/**
 * Retrieves the API key from the environment variables for the native platform.
 *
 * @return The API key as a [String].
 */
internal actual fun getApiKey(): String = getenv("GEMINI_API_KEY")?.toKString() ?: ""

/**
 * Retrieves an image as a base64 encoded string for the native platform.
 *
 * @return An empty string as a placeholder.
 */
internal actual fun getImage(): String = ""
