package io.github.ugaikit.gemini4kt.filesearch

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * Represents the file search lifecycle test.
 */
class FileSearchLifecycleTest {
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
    fun closeDoesNotCloseUnownedHttpClient() {
        val tracked = buildTrackingClient()
        val fileSearch = FileSearch(apiKey = "test-api-key", client = tracked.client)

        fileSearch.close()

        assertFalse(tracked.closed.isCompleted)
    }
}
