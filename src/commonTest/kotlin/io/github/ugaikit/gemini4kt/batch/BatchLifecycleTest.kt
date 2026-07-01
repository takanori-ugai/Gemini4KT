package io.github.ugaikit.gemini4kt.batch

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * Represents the batch lifecycle test.
 */
class BatchLifecycleTest {
    private data class TrackedClient(
        val client: HttpClient,
        val closed: CompletableDeferred<Unit>,
    )

    /**
     * Builds a client that reports when it is closed.
     */
    private fun buildTrackingClient(): TrackedClient {
        val closed = CompletableDeferred<Unit>()
        val client =
            HttpClient(MockEngine) {
                engine {
                    addHandler { respondOk() }
                }
            }
        client.coroutineContext[Job]?.invokeOnCompletion { closed.complete(Unit) }
        return TrackedClient(client, closed)
    }

    @Test
    fun closeDoesNotCloseUnownedHttpClient() =
        runTest {
            val tracked = buildTrackingClient()
            val batch = Batch(apiKey = "test-api-key", client = tracked.client)

            batch.close()

            assertFalse(tracked.closed.isCompleted)
        }
}
