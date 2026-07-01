package io.github.ugaikit.gemini4kt

import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SseEventsTest {
    @Test
    fun consumeServerSentEventsAggregatesMultiLinePayloads() =
        runTest {
            val payloads = mutableListOf<String>()
            val stream =
                """
                event: message
                data: {
                data:   "value": "hello"
                data: }
                """.trimIndent()

            ByteReadChannel(stream).consumeServerSentEvents { payloads.add(it) }

            assertEquals(listOf("{\n  \"value\": \"hello\"\n}"), payloads)
        }
}
