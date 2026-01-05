package io.github.ugaikit.gemini4kt.live

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeminiLiveTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    @Test
    fun processMessageCompletesHandshakeOnSetupComplete() =
        runTest {
            val handshakeCompleted = CompletableDeferred<Unit>()
            val incoming = Channel<BidiGenerateContentServerMessage>(1)
            val msg =
                BidiGenerateContentServerMessage(
                    setupComplete = BidiGenerateContentSetupComplete(),
                )
            val text = json.encodeToString(msg)

            callProcessMessage(text, handshakeCompleted, incoming)

            handshakeCompleted.await()
            val received = incoming.receive()
            assertTrue(received.setupComplete != null)
        }

    @Test
    fun processMessageDoesNotCompleteHandshakeBeforeSetup() =
        runTest {
            val handshakeCompleted = CompletableDeferred<Unit>()
            val incoming = Channel<BidiGenerateContentServerMessage>(1)
            val msg =
                BidiGenerateContentServerMessage(
                    serverContent = BidiGenerateContentServerContent(turnComplete = true),
                )
            val text = json.encodeToString(msg)

            callProcessMessage(text, handshakeCompleted, incoming)

            assertFalse(handshakeCompleted.isCompleted)
            val received = incoming.receive()
            assertTrue(received.serverContent?.turnComplete == true)
        }

    @Test
    fun processMessageCompletesHandshakeExceptionallyOnDecodeFailure() =
        runTest {
            val handshakeCompleted = CompletableDeferred<Unit>()
            val incoming = Channel<BidiGenerateContentServerMessage>(1)

            callProcessMessage("{invalid}", handshakeCompleted, incoming)

            assertTrue(handshakeCompleted.isCompleted)
            val result = runCatching { handshakeCompleted.await() }
            assertTrue(result.isFailure)
            assertTrue(incoming.tryReceive().isFailure)
        }

    private suspend fun callProcessMessage(
        text: String,
        handshakeCompleted: CompletableDeferred<Unit>,
        incoming: Channel<BidiGenerateContentServerMessage>,
    ) {
        val clazz = Class.forName("io.github.ugaikit.gemini4kt.live.GeminiLiveKt")
        val method =
            clazz.getDeclaredMethod(
                "processMessage",
                String::class.java,
                CompletableDeferred::class.java,
                Channel::class.java,
                Json::class.java,
                kotlin.coroutines.Continuation::class.java,
            )
        method.isAccessible = true
        val kfun = checkNotNull(method.kotlinFunction)
        kfun.isAccessible = true
        kfun.callSuspend(text, handshakeCompleted, incoming, json)
    }
}
