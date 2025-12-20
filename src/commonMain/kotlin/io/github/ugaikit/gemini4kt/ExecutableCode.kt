package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents the executable code.
 *
 * @property language The language.
 * @property code The code.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ExecutableCode(
    val language: String,
    val code: String,
)

/**
 * Represents the executable code builder.
 */
class ExecutableCodeBuilder {
    /**
     * Holds the language.
     */
    var language: String = ""

    /**
     * Holds the code.
     */
    var code: String = ""

    /**
     * Handles build.
     */
    fun build() = ExecutableCode(language, code)
}

/**
 * Handles executable code.
 *
 * @param init The init.
 */
fun executableCode(init: ExecutableCodeBuilder.() -> Unit): ExecutableCode = ExecutableCodeBuilder().apply(init).build()
