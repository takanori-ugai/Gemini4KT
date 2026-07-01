package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SchemaTest {
    private val json = Json { encodeDefaults = false }

    @Test
    fun schemaBuilderSerializesBooleanAdditionalProperties() {
        val schema =
            SchemaBuilder()
                .apply {
                    type = "object"
                    additionalProperties(false)
                }.build()

        val encoded = json.encodeToString(schema)

        assertEquals("""{"type":"object","additionalProperties":false}""", encoded)
    }

    @Test
    fun schemaSerializesBooleanAdditionalProperties() {
        val schema =
            Schema(
                type = "object",
                additionalProperties = AdditionalProperties.BooleanValue(false),
            )

        val encoded = json.encodeToString(schema)

        assertEquals("""{"type":"object","additionalProperties":false}""", encoded)
    }

    @Test
    fun schemaSerializesNestedSchemaAdditionalProperties() {
        val schema =
            Schema(
                type = "object",
                additionalProperties =
                    AdditionalProperties.SchemaValue(
                        Schema(type = "string"),
                    ),
            )

        val encoded = json.encodeToString(schema)

        assertEquals("""{"type":"object","additionalProperties":{"type":"string"}}""", encoded)
        assertIs<AdditionalProperties.SchemaValue>(schema.additionalProperties)
    }
}
