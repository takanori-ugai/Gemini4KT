package io.github.ugaikit.gemini4kt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@GeminiFunction(description = "A test function")
fun testFunction(
    @GeminiParameter(description = "A string parameter") str: String,
    @GeminiParameter(description = "An integer parameter") int: Int,
) {
}

fun functionWithoutAnnotation(
    str: String,
    int: Int,
) {
}

class FunctionDslTest {
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

    @Test
    fun `buildFunctionDeclaration throws exception for function without annotation`() {
        assertThrows<IllegalArgumentException> {
            buildFunctionDeclaration(::functionWithoutAnnotation)
        }
    }
}
