package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

expect fun createHttpClient(json: Json): HttpClient
