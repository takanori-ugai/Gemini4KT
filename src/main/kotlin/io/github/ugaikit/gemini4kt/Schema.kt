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
