package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Platform-agnostic options for the shared Gemini HTTP client configuration.
 */
data class GeminiHttpClientConfig(
    val requestTimeoutMillis: Long? = null,
    val usePlatformDefaultTimeout: Boolean = true,
    val installLogging: Boolean = false,
)

/**
 * Platform default request timeout for clients that opt into a timeout by default.
 */
internal expect val platformDefaultRequestTimeoutMillis: Long?

/**
 * Applies the shared Gemini HTTP client configuration on top of a target-specific engine.
 */
internal fun HttpClientConfig<*>.configureGeminiHttpClient(
    json: Json,
    config: GeminiHttpClientConfig = GeminiHttpClientConfig(),
) {
    install(ContentNegotiation) {
        json(json)
    }
    val requestTimeoutMillis =
        config.requestTimeoutMillis
            ?: if (config.usePlatformDefaultTimeout) platformDefaultRequestTimeoutMillis else null
    if (requestTimeoutMillis != null) {
        install(HttpTimeout) {
            this.requestTimeoutMillis = requestTimeoutMillis
        }
    }
    if (config.installLogging) {
        install(Logging) {
            level = LogLevel.NONE
        }
    }
}
