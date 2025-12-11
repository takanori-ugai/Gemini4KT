package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

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
