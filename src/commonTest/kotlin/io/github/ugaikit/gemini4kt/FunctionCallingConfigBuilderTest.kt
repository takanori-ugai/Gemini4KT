package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FunctionCallingConfigBuilderTest {
    @Test
    fun buildRequiresAllowedFunctionsForAnyMode() {
        val config =
            functionCallingConfig {
                mode = Mode.ANY
                allowFunction("search")
            }

        assertEquals(Mode.ANY, config.mode)
        assertEquals(listOf("search"), config.allowedFunctionNames.toList())
    }

    @Test
    fun buildRejectsAnyModeWithoutAllowedFunctions() {
        assertFailsWith<IllegalArgumentException> {
            functionCallingConfig {
                mode = Mode.ANY
            }
        }
    }

    @Test
    fun buildRejectsAllowedFunctionsForAutoMode() {
        assertFailsWith<IllegalArgumentException> {
            functionCallingConfig {
                mode = Mode.AUTO
                allowFunction("search")
            }
        }
    }

    @Test
    fun buildRequiresAllowedFunctionsForValidatedMode() {
        val config =
            functionCallingConfig {
                mode = Mode.VALIDATED
                allowFunction("search")
            }

        assertEquals(Mode.VALIDATED, config.mode)
        assertEquals(listOf("search"), config.allowedFunctionNames.toList())
    }

    @Test
    fun buildRejectsValidatedModeWithoutAllowedFunctions() {
        assertFailsWith<IllegalArgumentException> {
            functionCallingConfig {
                mode = Mode.VALIDATED
            }
        }
    }

    @Test
    fun buildAllowsEmptyAllowlistForNoneMode() {
        val config =
            functionCallingConfig {
                mode = Mode.NONE
            }

        assertEquals(Mode.NONE, config.mode)
        assertEquals(emptyList<String>(), config.allowedFunctionNames.toList())
    }

    @Test
    fun buildAllowsEmptyAllowlistForUnspecifiedMode() {
        val config =
            functionCallingConfig {
                mode = Mode.MODE_UNSPECIFIED
            }

        assertEquals(Mode.MODE_UNSPECIFIED, config.mode)
        assertEquals(emptyList<String>(), config.allowedFunctionNames.toList())
    }
}
