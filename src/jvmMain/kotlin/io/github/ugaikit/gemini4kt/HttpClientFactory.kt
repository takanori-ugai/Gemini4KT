package io.github.ugaikit.gemini4kt

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createHttpClient(json: Json): HttpClient =
    HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }
