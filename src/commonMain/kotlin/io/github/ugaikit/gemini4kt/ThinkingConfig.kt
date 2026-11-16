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
 * @property thinkingBudget Legacy budget for models that support it. The default is omitted,
 * including when encoding defaults; use [thinkingLevel] for Gemini 4 Hydrogen.
 * @property thinkingLevel The thinking level.
 * @property includeThoughts The flag to include thoughts.
 */
@OptIn(ExperimentalJsExport::class, ExperimentalSerializationApi::class)
@JsExport
@Serializable
data class ThinkingConfig(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("thinking_budget")
    val thinkingBudget: Int = 1024,
    @SerialName("thinking_level")
    val thinkingLevel: ThinkingLevel? = null,
    @SerialName("include_thoughts")
    val includeThoughts: Boolean? = null,
)
