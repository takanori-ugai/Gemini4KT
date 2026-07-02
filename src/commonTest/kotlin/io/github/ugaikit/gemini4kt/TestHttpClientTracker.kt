package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient

internal class TestHttpClientTracker {
    private val clients = mutableListOf<HttpClient>()

    fun track(client: HttpClient): HttpClient {
        clients.add(client)
        return client
    }

    fun closeAll() {
        clients.forEach(HttpClient::close)
        clients.clear()
    }
}
