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
        val labelsSchema = assertNotNull(declaration.parameters.properties["labels"])
        val additionalProperties = assertNotNull(labelsSchema.additionalProperties) as AdditionalProperties.SchemaValue

        assertEquals("object", labelsSchema.type)
        assertEquals("string", additionalProperties.schema.type)
    }
}
