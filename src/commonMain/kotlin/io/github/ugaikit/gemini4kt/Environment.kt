package io.github.ugaikit.gemini4kt

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents the environment being operated, such as a web browser.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class Environment {
    ENVIRONMENT_UNSPECIFIED,
    ENVIRONMENT_BROWSER,
}
