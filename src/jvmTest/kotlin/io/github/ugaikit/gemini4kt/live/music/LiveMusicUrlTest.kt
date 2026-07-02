package io.github.ugaikit.gemini4kt.live.music

import java.lang.reflect.InvocationTargetException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Exercises the LiveMusic websocket URL builder through reflection.
 */
class LiveMusicUrlTest {
    /**
     * Verifies scheme normalization and apiVersion handling.
     */
    @Test
    fun buildWebSocketUrlHandlesProtocols() {
        val buildUrl =
            Class
                .forName("io.github.ugaikit.gemini4kt.live.music.LiveMusicKt")
                .getDeclaredMethod("buildWebSocketUrl", LiveMusicOptions::class.java)
                .apply { isAccessible = true }

        fun invoke(options: LiveMusicOptions): String = buildUrl.invoke(null, options) as String

        assertEquals(
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage." +
                "v1alpha.GenerativeService.BidiGenerateMusic",
            invoke(LiveMusicOptions()),
        )
        assertEquals(
            "ws://localhost:8080/ws/google.ai.generativelanguage." +
                "v1beta.GenerativeService.BidiGenerateMusic",
            invoke(LiveMusicOptions(apiVersion = "v1beta", baseUrl = "http://localhost:8080", allowCustomEndpoint = true)),
        )
        assertEquals(
            "wss://custom-host/ws/google.ai.generativelanguage." +
                "v1alpha.GenerativeService.BidiGenerateMusic",
            invoke(LiveMusicOptions(baseUrl = "custom-host/", allowCustomEndpoint = true)),
        )
        assertFailsWith<InvocationTargetException> {
            invoke(LiveMusicOptions(baseUrl = "custom-host/"))
        }
    }
}
