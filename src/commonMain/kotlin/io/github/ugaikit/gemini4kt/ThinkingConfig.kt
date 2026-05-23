package io.github.ugaikit.gemini4kt

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents the thinking config.
 *
 * @property thinkingBudget The thinking budget.
 * @property thinkingLevel The thinking level.
 * @property includeThoughts The flag to include thoughts.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ThinkingConfig(
    @ExperimentalSerializationApi
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName("thinking_budget")
    val thinkingBudget: Int = 1024,
    @SerialName("thinking_level")
    val thinkingLevel: String? = null,
    @SerialName("include_thoughts")
    val includeThoughts: Boolean? = null,
)
