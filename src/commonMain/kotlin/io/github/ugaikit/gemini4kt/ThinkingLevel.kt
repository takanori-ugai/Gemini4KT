package io.github.ugaikit.gemini4kt

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Allow user to specify how much to think using enum instead of integer budget.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ThinkingLevel {
    THINKING_LEVEL_UNSPECIFIED,
    MINIMAL,
    LOW,
    MEDIUM,
    HIGH,
}
