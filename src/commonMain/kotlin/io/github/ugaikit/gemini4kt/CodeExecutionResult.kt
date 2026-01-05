package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents the code execution result.
 *
 * @property outcome The outcome.
 * @property output The output.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CodeExecutionResult(
    val outcome: String,
    val output: String,
)

/**
 * Represents the code execution result builder.
 */
class CodeExecutionResultBuilder {
    /**
     * Holds the outcome.
     */
    var outcome: String = ""

    /**
     * Holds the output.
     */
    var output: String = ""

    /**
     * Handles build.
     */
    fun build() = CodeExecutionResult(outcome, output)
}

/**
 * Handles code execution result.
 *
 * @param init The init.
 */
fun codeExecutionResult(init: CodeExecutionResultBuilder.() -> Unit): CodeExecutionResult = CodeExecutionResultBuilder().apply(init).build()
