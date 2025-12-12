package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

expect class FileUploadProviderImpl(
    apiKey: String,
    client: HttpClient? = null,
    json: Json = Json { ignoreUnknownKeys = true },
) : FileUploadProvider
