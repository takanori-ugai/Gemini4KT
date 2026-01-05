package io.github.ugaikit.gemini4kt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests test function.
 *
 * @param str The str.
 * @param int The int.
 */
@GeminiFunction(description = "A test function")
@Suppress("EmptyFunctionBlock", "UnusedParameter")
fun testFunction(
    @GeminiParameter(description = "A string parameter") str: String,
    @GeminiParameter(description = "An integer parameter") int: Int,
) {
}

/**
 * Handles function without annotation.
 *
 * @param str The str.
 * @param int The int.
 */
@Suppress("EmptyFunctionBlock", "UnusedParameter")
fun functionWithoutAnnotation(
    str: String,
    int: Int,
) {
}

/**
 * Represents the function dsl test.
 */
class FunctionDslTest {
    /**
     * Handles build function declaration.
     */
    @Test
    fun `buildFunctionDeclaration generates correct declaration for annotated function`() {
        val declaration = buildFunctionDeclaration(::testFunction)
        assertEquals("testFunction", declaration.name)
        assertEquals("A test function", declaration.description)

        val parameters = declaration.parameters
        assertEquals("object", parameters.type)
        assertEquals(2, parameters.properties.size)
        assertEquals(listOf("str", "int"), parameters.required)

        val strParam = parameters.properties["str"]!!
        assertEquals("string", strParam.type)
        assertEquals("A string parameter", strParam.description)

        val intParam = parameters.properties["int"]!!
        assertEquals("integer", intParam.type)
        assertEquals("An integer parameter", intParam.description)
    }

    /**
     * Handles build function declaration.
     */
    @Test
    fun `buildFunctionDeclaration throws exception for function without annotation`() {
        assertThrows<IllegalArgumentException> {
            buildFunctionDeclaration(::functionWithoutAnnotation)
        }
    }
}
