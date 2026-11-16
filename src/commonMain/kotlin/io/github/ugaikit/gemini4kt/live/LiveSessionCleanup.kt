package io.github.ugaikit.gemini4kt.live

import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeoutOrNull

internal const val LIVE_SESSION_CLOSE_TIMEOUT_MS = 5_000L

/** Bounds graceful closure so stalled output cannot prevent the remaining session cleanup. */
internal suspend fun WebSocketSession.closeLiveWebSocket() {
    val closed =
        withTimeoutOrNull(LIVE_SESSION_CLOSE_TIMEOUT_MS) {
            close()
            // Ktor close catches cancellation while sending/flushing; restore the timeout signal.
            currentCoroutineContext().ensureActive()
            true
        }
    if (closed != true) {
        cancel("Timed out closing the live WebSocket.")
    }
}
