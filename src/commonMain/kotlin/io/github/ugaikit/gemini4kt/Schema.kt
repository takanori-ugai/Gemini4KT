package io.github.ugaikit.gemini4kt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
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
 * @property additionalProperties Schema or boolean flag for map values when the schema
 * represents an object map.
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
    val additionalProperties: AdditionalProperties? = null,
)

/**
 * Represents the additional properties value of a schema.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable(with = AdditionalPropertiesSerializer::class)
sealed interface AdditionalProperties {
    /**
     * Represents a boolean additionalProperties value.
     */
    @Serializable
    data class BooleanValue(
        val value: Boolean,
    ) : AdditionalProperties

    /**
     * Represents a schema additionalProperties value.
     */
    @Serializable
    data class SchemaValue(
        val schema: Schema,
    ) : AdditionalProperties
}

/**
 * Serializes additionalProperties as either a boolean or a nested schema object.
 */
@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
object AdditionalPropertiesSerializer : KSerializer<AdditionalProperties> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("io.github.ugaikit.gemini4kt.AdditionalProperties", SerialKind.CONTEXTUAL)

    override fun serialize(
        encoder: Encoder,
        value: AdditionalProperties,
    ) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: throw SerializationException("AdditionalProperties can only be serialized as JSON.")

        when (value) {
            is AdditionalProperties.BooleanValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is AdditionalProperties.SchemaValue -> jsonEncoder.encodeSerializableValue(Schema.serializer(), value.schema)
        }
    }

    override fun deserialize(decoder: Decoder): AdditionalProperties {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("AdditionalProperties can only be deserialized as JSON.")

        val element = jsonDecoder.decodeJsonElement()
        return when (element) {
            is JsonPrimitive ->
                element.booleanOrNull?.let { AdditionalProperties.BooleanValue(it) }
                    ?: throw SerializationException("Expected additionalProperties to be a boolean or schema.")
            is JsonObject ->
                AdditionalProperties.SchemaValue(
                    jsonDecoder.json.decodeFromJsonElement(Schema.serializer(), element),
                )
            else -> throw SerializationException("Expected additionalProperties to be a boolean or schema object.")
        }
    }
}

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
     * Holds the additional properties.
     */
    private var additionalPropertiesInternal: AdditionalProperties? = null

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
     * Handles additional properties.
     *
     * @param init The init.
     */
    fun additionalProperties(init: SchemaBuilder.() -> Unit) {
        additionalPropertiesInternal = AdditionalProperties.SchemaValue(SchemaBuilder().apply(init).build())
    }

    /**
     * Handles additional properties as a boolean flag.
     *
     * @param value The boolean value.
     */
    fun additionalProperties(value: Boolean) {
        additionalPropertiesInternal = AdditionalProperties.BooleanValue(value)
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
            additionalProperties = additionalPropertiesInternal,
        )
    }
}
