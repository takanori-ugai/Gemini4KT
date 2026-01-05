package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * Handles create http client.
 *
 * @param json The json.
 */
expect fun createHttpClient(json: Json): HttpClient
