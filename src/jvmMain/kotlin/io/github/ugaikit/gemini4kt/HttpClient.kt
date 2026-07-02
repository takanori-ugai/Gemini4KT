package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.serialization.json.Json

/**
 * Handles create http client.
 *
 * @param json The json.
 * @param config Optional shared client configuration.
 */
actual fun createHttpClient(
    json: Json,
    config: GeminiHttpClientConfig,
): HttpClient =
    HttpClient(CIO) {
        configureGeminiHttpClient(json, config)
    }

internal actual val platformDefaultRequestTimeoutMillis: Long? = 60_000
