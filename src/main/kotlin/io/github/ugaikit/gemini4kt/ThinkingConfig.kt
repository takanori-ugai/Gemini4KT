package io.github.ugaikit.gemini4kt

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class ThinkingConfig(
    @ExperimentalSerializationApi
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val thinkingBudget: Int = 1024,
)
