package io.github.ugaikit.gemini4kt.live

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.Frame
import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketFrameDecodersTest {
    private val logger = KotlinLogging.logger("WebSocketFrameDecodersTest")

    @Test
    fun decodeBinaryFrameDecodesUtf8Payload() {
        val frame = Frame.Binary(fin = true, data = "hello".encodeToByteArray())

        val decoded = decodeBinaryFrame(frame, logger)

        assertEquals("hello", decoded)
    }

    @Test
    fun decodeBinaryFrameDecodesEmptyPayload() {
        val frame = Frame.Binary(fin = true, data = byteArrayOf())

        val decoded = decodeBinaryFrame(frame, logger)

        assertEquals("", decoded)
    }
}
