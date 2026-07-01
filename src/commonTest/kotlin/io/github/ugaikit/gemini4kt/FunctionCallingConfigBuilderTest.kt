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
}
