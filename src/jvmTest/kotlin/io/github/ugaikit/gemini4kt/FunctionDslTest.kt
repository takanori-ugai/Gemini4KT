package io.github.ugaikit.gemini4kt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@GeminiFunction(description = "Reads a labeled map")
private fun mapReader(
    @GeminiParameter(description = "labels") labels: Map<String, String>,
): String = labels["mode"] ?: "unknown"

class FunctionDslTest {
    @Test
    fun buildFunctionDeclarationIncludesMapValueSchema() {
        val declaration = buildFunctionDeclaration(::mapReader)
        val labelsSchema = declaration.parameters.properties["labels"]

        assertNotNull(labelsSchema)
        assertEquals("object", labelsSchema.type)
        assertNotNull(labelsSchema.additionalProperties)
        assertEquals("string", checkNotNull(labelsSchema.additionalProperties).type)
    }
}
