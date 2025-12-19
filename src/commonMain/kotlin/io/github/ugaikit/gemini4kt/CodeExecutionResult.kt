package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class CodeExecutionResult(
    val outcome: String,
    val output: String,
)

class CodeExecutionResultBuilder {
    var outcome: String = ""
    var output: String = ""

    fun build() = CodeExecutionResult(outcome, output)
}

fun codeExecutionResult(init: CodeExecutionResultBuilder.() -> Unit): CodeExecutionResult = CodeExecutionResultBuilder().apply(init).build()
