package io.github.ugaikit.gemini4kt.genie

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.js.Promise

/** Browser WebRTC peer that presents Genie 4's media on a caller-provided video element. */
class BrowserGenieWebRtcPeer(
    private val videoElement: dynamic,
) : GenieWebRtcPeer {
    private val peer: dynamic = js("new RTCPeerConnection()")
    private val media: dynamic = js("new MediaStream()")
    private val incoming = Channel<String>(Channel.BUFFERED)
    private val channelOpen = CompletableDeferred<Unit>()
    private val channel: dynamic

    override val events = incoming.receiveAsFlow()

    init {
        peer.addTransceiver("audio", js("({direction:'recvonly'})"))
        peer.addTransceiver("video", js("({direction:'recvonly'})"))
        peer.ontrack = { event: dynamic ->
            media.addTrack(event.track)
            Unit
        }
        videoElement.srcObject = media
        channel = peer.createDataChannel("events")
        channel.onopen = { _: dynamic ->
            channelOpen.complete(Unit)
            Unit
        }
        channel.onmessage = { event: dynamic ->
            val text = event.data as? String
            if (text == null || incoming.trySend(text).isFailure) {
                fail("Could not receive a Genie WebRTC event.")
            }
            Unit
        }
        channel.onerror = { _: dynamic ->
            fail("Genie WebRTC data channel failed.")
            Unit
        }
        channel.onclose = { _: dynamic ->
            channelOpen.completeExceptionally(IllegalStateException("Genie WebRTC data channel closed."))
            incoming.close()
            Unit
        }
    }

    override suspend fun createOffer(): String {
        val offer = peer.createOffer().unsafeCast<Promise<dynamic>>().await()
        peer.setLocalDescription(offer).unsafeCast<Promise<Unit>>().await()
        return peer.localDescription.sdp as String
    }

    override suspend fun acceptAnswer(sdp: String) {
        val answer: dynamic = js("({type:'answer'})")
        answer.sdp = sdp
        peer.setRemoteDescription(answer).unsafeCast<Promise<Unit>>().await()
        channelOpen.await()
    }

    override suspend fun send(message: String) {
        check(channel.readyState == "open") { "Genie WebRTC data channel is closed." }
        channel.send(message)
    }

    override suspend fun close() {
        channel.close()
        peer.close()
        incoming.close()
        if (videoElement.srcObject === media) videoElement.srcObject = null
        channelOpen.cancel()
    }

    private fun fail(message: String) {
        val failure = IllegalStateException(message)
        channelOpen.completeExceptionally(failure)
        incoming.close(failure)
        channel.close()
    }
}
