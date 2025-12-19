package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ExecutableCode(
    val language: String,
    val code: String,
)

class ExecutableCodeBuilder {
    var language: String = ""
    var code: String = ""

    fun build() = ExecutableCode(language, code)
}

fun executableCode(init: ExecutableCodeBuilder.() -> Unit): ExecutableCode = ExecutableCodeBuilder().apply(init).build()
