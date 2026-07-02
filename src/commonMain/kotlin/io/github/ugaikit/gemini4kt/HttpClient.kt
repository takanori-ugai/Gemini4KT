package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * Handles create http client.
 *
 * @param json The json.
 * @param config Optional shared client configuration.
 */
expect fun createHttpClient(
    json: Json,
    config: GeminiHttpClientConfig = GeminiHttpClientConfig(),
): HttpClient
