package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents the schema definition for a data model, detailing its structure,
 * type, and various constraints.
 *
 * @property type The primary type of data this schema represents (e.g., "string",
 * "integer", "object").
 * @property format Additional formatting information for the data type, providing
 * more precise definition (e.g., "date-time" for string type).
 * @property description A human-readable description of the schema, explaining its
 * purpose and usage.
 * @property nullable Indicates whether the value can be null.
 * @property enum A list of acceptable values for this schema if it represents an
 * enumeration type.
 * @property properties A map of property names to their respective schemas, defining
 * the structure of an object type. Each entry represents a field and its schema.
 * @property required A list of property names that are required for this schema,
 * ensuring certain fields must be present in the data.
 * @property items The schema for items in an array, applicable when the type is
 * "array". Defines the schema of elements within the array.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class Schema(
    val type: String,
    val format: String? = null,
    val description: String? = null,
    val nullable: Boolean = false,
    val enum: List<String> = emptyList(),
    val properties: Map<String, Schema> = emptyMap(),
    val required: List<String> = emptyList(),
    val items: Schema? = null,
)

/**
 * Represents the schema builder.
 */
class SchemaBuilder {
    /**
     * Holds the type.
     */
    var type: String = "object" // Default type

    /**
     * Holds the format.
     */
    var format: String? = null

    /**
     * Holds the description.
     */
    var description: String? = null

    /**
     * Holds the nullable.
     */
    var nullable: Boolean = false

    /**
     * Holds the enum internal.
     */
    private val enumInternal: MutableList<String> = mutableListOf()

    /**
     * Holds the properties internal.
     */
    private val propertiesInternal: MutableMap<String, SchemaBuilder.() -> Unit> = mutableMapOf()

    /**
     * Holds the required internal.
     */
    private val requiredInternal: MutableList<String> = mutableListOf()

    /**
     * Holds the items.
     */
    var items: SchemaBuilder? = null

    /**
     * Handles enum.
     *
     * @param values The values.
     */
    fun enum(vararg values: String) {
        enumInternal.addAll(values)
    }

    /**
     * Handles property.
     *
     * @param name The name.
     * @param init The init.
     */
    fun property(
        name: String,
        init: SchemaBuilder.() -> Unit,
    ) {
        propertiesInternal[name] = init
    }

    /**
     * Handles required.
     *
     * @param fields The fields.
     */
    fun required(vararg fields: String) {
        requiredInternal.addAll(fields)
    }

    /**
     * Handles items.
     *
     * @param init The init.
     */
    fun items(init: SchemaBuilder.() -> Unit) {
        items = SchemaBuilder().apply(init)
    }

    /**
     * Handles build.
     */
    fun build(): Schema {
        val properties = propertiesInternal.mapValues { SchemaBuilder().apply(it.value).build() }
        return Schema(
            type = type,
            format = format,
            description = description,
            nullable = nullable,
            enum = enumInternal,
            properties = properties,
            required = requiredInternal,
            items = items?.build(),
        )
    }
}
