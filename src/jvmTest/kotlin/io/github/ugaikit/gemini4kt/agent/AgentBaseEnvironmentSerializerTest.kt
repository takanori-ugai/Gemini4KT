package io.github.ugaikit.gemini4kt.agent

import io.mockk.mockk
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AgentBaseEnvironmentSerializerTest {
    @Test
    fun serializeRejectsNonJsonEncoder() {
        assertFailsWith<SerializationException> {
            AgentBaseEnvironmentSerializer.serialize(mockk<Encoder>(), AgentEnvironmentReference("env_123"))
        }
    }

    @Test
    fun deserializeRejectsNonJsonDecoder() {
        assertFailsWith<SerializationException> {
            AgentBaseEnvironmentSerializer.deserialize(mockk<Decoder>())
        }
    }
}
