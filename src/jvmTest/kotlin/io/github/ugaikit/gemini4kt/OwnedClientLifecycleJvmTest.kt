package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.batch.Batch
import io.github.ugaikit.gemini4kt.filesearch.FileSearch
import io.ktor.client.HttpClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertTrue

class OwnedClientLifecycleJvmTest {
    private suspend fun assertOwnedHttpClientCloses(owner: Any) {
        val field = owner.javaClass.getDeclaredField("httpClient")
        field.isAccessible = true
        val httpClient = field.get(owner) as HttpClient
        val closed = CompletableDeferred<Unit>()
        httpClient.coroutineContext[Job]?.invokeOnCompletion { closed.complete(Unit) }

        when (owner) {
            is Gemini -> owner.close()
            is GeminiAI -> owner.close()
            is Batch -> owner.close()
            is FileSearch -> owner.close()
        }

        withTimeout(5_000) { closed.await() }
        assertTrue(closed.isCompleted)
    }

    @Test
    fun geminiClosesOwnedHttpClient() =
        runTest {
            assertOwnedHttpClientCloses(Gemini(apiKey = "test-api-key"))
        }

    @Test
    fun geminiAICloseOwnedHttpClient() =
        runTest {
            assertOwnedHttpClientCloses(GeminiAI(apiKey = "test-api-key"))
        }

    @Test
    fun batchClosesOwnedHttpClient() =
        runTest {
            assertOwnedHttpClientCloses(Batch(apiKey = "test-api-key"))
        }

    @Test
    fun fileSearchClosesOwnedHttpClient() =
        runTest {
            assertOwnedHttpClientCloses(FileSearch(apiKey = "test-api-key"))
        }
}
