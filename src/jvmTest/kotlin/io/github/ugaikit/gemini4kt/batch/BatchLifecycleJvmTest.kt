package io.github.ugaikit.gemini4kt.batch

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Represents the batch lifecycle JVM test.
 */
class BatchLifecycleJvmTest {
    private data class TrackedClient(
        val closed: CompletableDeferred<Unit>,
    )

    /**
     * Extracts the private HTTP client from the batch instance and tracks its close event.
     */
    private fun trackOwnedClient(batch: Batch): TrackedClient {
        val field = Batch::class.java.getDeclaredField("httpClient")
        field.isAccessible = true
        val client = field.get(batch) as io.ktor.client.HttpClient

        val closed = CompletableDeferred<Unit>()
        client.coroutineContext[Job]?.invokeOnCompletion { closed.complete(Unit) }
        return TrackedClient(closed)
    }

    @Test
    fun closeClosesOwnedHttpClient() {
        val batch = Batch(apiKey = "test-api-key")
        val tracked = trackOwnedClient(batch)

        batch.close()

        assertTrue(tracked.closed.isCompleted)
    }
}
