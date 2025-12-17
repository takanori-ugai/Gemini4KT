package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

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
@Serializable
@JsExport
data class Schema(
    val type: String,
    val format: String? = null,
    val description: String? = null,
    val nullable: Boolean = false,
    val enum: Array<String> = emptyArray(),
    val properties: Map<String, Schema> = emptyMap(),
    val required: Array<String> = emptyArray(),
    val items: Schema? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Schema

        if (type != other.type) return false
        if (format != other.format) return false
        if (description != other.description) return false
        if (nullable != other.nullable) return false
        if (!enum.contentEquals(other.enum)) return false
        if (properties != other.properties) return false
        if (!required.contentEquals(other.required)) return false
        if (items != other.items) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (format?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + nullable.hashCode()
        result = 31 * result + enum.contentHashCode()
        result = 31 * result + properties.hashCode()
        result = 31 * result + required.contentHashCode()
        result = 31 * result + (items?.hashCode() ?: 0)
        return result
    }
}

class SchemaBuilder {
    var type: String = "object" // Default type
    var format: String? = null
    var description: String? = null
    var nullable: Boolean = false
    private val enumInternal: MutableList<String> = mutableListOf()
    private val propertiesInternal: MutableMap<String, SchemaBuilder.() -> Unit> = mutableMapOf()
    private val requiredInternal: MutableList<String> = mutableListOf()
    var items: SchemaBuilder? = null

    fun enum(vararg values: String) {
        enumInternal.addAll(values)
    }

    fun property(
        name: String,
        init: SchemaBuilder.() -> Unit,
    ) {
        propertiesInternal[name] = init
    }

    fun required(vararg fields: String) {
        requiredInternal.addAll(fields)
    }

    fun items(init: SchemaBuilder.() -> Unit) {
        items = SchemaBuilder().apply(init)
    }

    fun build(): Schema {
        val properties = propertiesInternal.mapValues { SchemaBuilder().apply(it.value).build() }
        return Schema(
            type = type,
            format = format,
            description = description,
            nullable = nullable,
            enum = enumInternal.toTypedArray(),
            properties = properties,
            required = requiredInternal.toTypedArray(),
            items = items?.build(),
        )
    }
}
