package io.github.ugaikit.gemini4kt

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class ThinkingConfig(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val thinkingBudget: Int = 1024,
)
