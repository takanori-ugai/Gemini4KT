package io.github.ugaikit.gemini4kt

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Regression coverage for JVM HTTP client configuration.
 */
class HttpClientConfigJvmTest {
    @Test
    fun configureGeminiHttpClientHonorsExplicitRequestTimeout() =
        runTest {
            val client =
                HttpClient(MockEngine) {
                    configureGeminiHttpClient(
                        Json { ignoreUnknownKeys = true },
                        GeminiHttpClientConfig(requestTimeoutMillis = 1, usePlatformDefaultTimeout = false),
                    )
                    engine {
                        addHandler {
                            delay(50)
                            respondOk()
                        }
                    }
                }

            try {
                assertFailsWith<HttpRequestTimeoutException> {
                    client.get("https://example.com/test")
                }
            } finally {
                client.close()
            }
        }
}
