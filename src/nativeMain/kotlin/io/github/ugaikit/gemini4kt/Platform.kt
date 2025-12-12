package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import platform.posix.getenv
import kotlinx.cinterop.toKString
import kotlinx.cinterop.ExperimentalForeignApi

actual fun createHttpClient(json: Json): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.NONE
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun getApiKey(): String {
    return getenv("GEMINI_API_KEY")?.toKString() ?: ""
}
