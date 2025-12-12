package kotlin.io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.Json
import platform.posix.getenv

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
internal actual fun getApiKey(): String = getenv("GEMINI_API_KEY")?.toKString() ?: ""
