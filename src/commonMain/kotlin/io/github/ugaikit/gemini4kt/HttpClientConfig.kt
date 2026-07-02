package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Applies the shared Gemini HTTP client configuration on top of a target-specific engine.
 */
internal fun HttpClientConfig<*>.configureGeminiHttpClient(
    json: Json,
    installTimeout: Boolean = false,
    installLogging: Boolean = false,
) {
    install(ContentNegotiation) {
        json(json)
    }
    if (installTimeout) {
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
        }
    }
    if (installLogging) {
        install(Logging) {
            level = LogLevel.NONE
        }
    }
}
