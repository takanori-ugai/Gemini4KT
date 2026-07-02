package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.serialization.json.Json

/**
 * Handles create http client.
 *
 * @param json The json.
 */
actual fun createHttpClient(json: Json): HttpClient =
    HttpClient(Android) {
        configureGeminiHttpClient(json, installTimeout = true)
    }
