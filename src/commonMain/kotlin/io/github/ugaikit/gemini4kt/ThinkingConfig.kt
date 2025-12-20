package io.github.ugaikit.gemini4kt

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents the thinking config.
 *
 * @property thinkingBudget The thinking budget.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ThinkingConfig(
    @ExperimentalSerializationApi
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val thinkingBudget: Int = 1024,
)
