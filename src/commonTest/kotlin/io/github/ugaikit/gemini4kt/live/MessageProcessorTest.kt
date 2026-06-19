package io.github.ugaikit.gemini4kt.live

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MessageProcessorTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val logger = KotlinLogging.logger("MessageProcessorTest")

    @Test
    fun processHandshakeMessageCompletesHandshakeForSetupComplete() =
        runTest {
            val handshakeCompleted = CompletableDeferred<Unit>()
            val incomingMessages = Channel<BidiGenerateContentServerMessage>(Channel.UNLIMITED)

            processHandshakeMessage(
                text = """{"setupComplete":{}}""",
                handshakeCompleted = handshakeCompleted,
                incomingMessages = incomingMessages,
                json = json,
                logger = logger,
            ) { message: BidiGenerateContentServerMessage ->
                message.setupComplete != null
            }

            assertTrue(handshakeCompleted.isCompleted)
            val decoded = incomingMessages.receive()
            assertNotNull(decoded.setupComplete)
        }

    @Test
    fun processHandshakeMessageForNonSetupMessageDoesNotCompleteHandshake() =
        runTest {
            val handshakeCompleted = CompletableDeferred<Unit>()
            val incomingMessages = Channel<BidiGenerateContentServerMessage>(Channel.UNLIMITED)

            processHandshakeMessage(
                text = """{"serverContent":{"turnComplete":true}}""",
                handshakeCompleted = handshakeCompleted,
                incomingMessages = incomingMessages,
                json = json,
                logger = logger,
            ) { message: BidiGenerateContentServerMessage ->
                message.setupComplete != null
            }

            assertFalse(handshakeCompleted.isCompleted)
            val decoded = incomingMessages.receive()
            assertEquals(true, decoded.serverContent?.turnComplete)
        }

    @Test
    fun processHandshakeMessageCompletesExceptionallyOnInvalidJson() =
        runTest {
            val handshakeCompleted = CompletableDeferred<Unit>()
            val incomingMessages = Channel<BidiGenerateContentServerMessage>(Channel.UNLIMITED)

            processHandshakeMessage(
                text = "{invalid-json",
                handshakeCompleted = handshakeCompleted,
                incomingMessages = incomingMessages,
                json = json,
                logger = logger,
            ) { message: BidiGenerateContentServerMessage ->
                message.setupComplete != null
            }

            assertTrue(handshakeCompleted.isCompleted)
            assertFailsWith<Throwable> {
                handshakeCompleted.await()
            }
            assertTrue(incomingMessages.tryReceive().isFailure)
        }
}
