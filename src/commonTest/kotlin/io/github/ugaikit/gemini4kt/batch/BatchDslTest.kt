package io.github.ugaikit.gemini4kt.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BatchDslTest {
    @Test
    fun batchInputConfigSupportsGcsSource() {
        val config =
            batchInputConfig {
                gcsSource("gs://bucket/input.jsonl")
            }

        assertEquals(listOf("gs://bucket/input.jsonl"), config.gcsSource?.uris)
    }

    @Test
    fun batchInputConfigSupportsFileName() {
        val config =
            batchInputConfig {
                fileName("files/input.jsonl")
            }

        assertEquals("files/input.jsonl", config.fileName)
    }

    @Test
    fun batchInputConfigRejectsMultipleInputModes() {
        assertFailsWith<IllegalArgumentException> {
            batchInputConfig {
                fileName("files/input.jsonl")
                requests {
                    request {
                        request(
                            io.github.ugaikit.gemini4kt.GenerateContentRequest(
                                contents = emptyArray(),
                            ),
                        )
                    }
                }
            }
        }
    }
}
