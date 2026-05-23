package io.github.ugaikit.gemini4kt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.full.memberFunctions

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

@GeminiFunction(description = "Collection parameter function")
@Suppress("EmptyFunctionBlock", "UnusedParameter")
fun functionWithCollections(
    @GeminiParameter(description = "Integer list") values: List<Int>,
    @GeminiParameter(description = "String map") tags: Map<String, String>,
) {
}

@GeminiFunction(description = "Function with optional argument")
@Suppress("EmptyFunctionBlock", "UnusedParameter")
fun functionWithOptional(
    @GeminiParameter(description = "Required value") required: String,
    @GeminiParameter(description = "Optional value") optional: String = "default",
) {
}

object OverloadedFunctions {
    @GeminiFunction(description = "Overloaded by int")
    fun sameName(
        @GeminiParameter(description = "number") value: Int,
    ): String = value.toString()

    @GeminiFunction(description = "Overloaded by string")
    fun sameName(
        @GeminiParameter(description = "text") value: String,
    ): String = value
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

    @Test
    fun `buildFunctionDeclaration supports list and map parameter types`() {
        val declaration = buildFunctionDeclaration(::functionWithCollections)
        val parameters = declaration.parameters

        val valuesParam = parameters.properties["values"]!!
        assertEquals("array", valuesParam.type)
        assertEquals("integer", valuesParam.items?.type)

        val tagsParam = parameters.properties["tags"]!!
        assertEquals("object", tagsParam.type)
    }

    @Test
    fun `buildFunctionDeclaration excludes optional parameters from required list`() {
        val declaration = buildFunctionDeclaration(::functionWithOptional)

        assertEquals(listOf("required"), declaration.parameters.required)
    }

    @Test
    fun `buildAutomaticFunctionBinding rejects duplicate function names`() {
        val overloadedFunctions =
            OverloadedFunctions::class
                .memberFunctions
                .filter { it.name == "sameName" }
                .toTypedArray()

        val exception =
            assertThrows<IllegalArgumentException> {
                buildAutomaticFunctionBinding(overloadedFunctions)
            }
        assertEquals(
            "Function names must be unique for automatic binding: sameName.",
            exception.message,
        )
    }
}
