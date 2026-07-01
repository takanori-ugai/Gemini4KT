package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Represents the file search lifecycle JVM test.
 */
class FileSearchLifecycleJvmTest {
    private data class TrackedClient(
        val closed: CompletableDeferred<Unit>,
    )

    /**
     * Extracts the private HTTP client from the file search instance and tracks its close event.
     */
    private fun trackOwnedClient(fileSearch: FileSearch): TrackedClient {
        val field = FileSearch::class.java.getDeclaredField("httpClient")
        field.isAccessible = true
        val client = field.get(fileSearch) as io.ktor.client.HttpClient

        val closed = CompletableDeferred<Unit>()
        client.coroutineContext[Job]?.invokeOnCompletion { closed.complete(Unit) }
        return TrackedClient(closed)
    }

    @Test
    fun closeClosesOwnedHttpClient() {
        val fileSearch = FileSearch(apiKey = "test-api-key")
        val tracked = trackOwnedClient(fileSearch)

        fileSearch.close()

        assertTrue(tracked.closed.isCompleted)
    }
}
