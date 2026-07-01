package io.github.ugaikit.gemini4kt

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.serializer
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters

/**
 * Builds a schema for a reflected Kotlin type.
 *
 * @param type The type.
 */
@OptIn(InternalSerializationApi::class)
private fun buildTypeSchema(type: KType): Schema {
    val classifier = type.classifier
    val baseSchema =
        when (classifier) {
            Int::class, Long::class -> Schema(type = "integer")
            String::class -> Schema(type = "string")
            Boolean::class -> Schema(type = "boolean")
            Double::class, Float::class -> Schema(type = "number")
            List::class, MutableList::class, Set::class, MutableSet::class, Collection::class, Iterable::class -> {
                val elementType =
                    type.arguments.firstOrNull()?.type
                        ?: throw IllegalArgumentException(
                            "Collection parameter type must declare an element type: $type",
                        )
                Schema(type = "array", items = buildTypeSchema(elementType))
            }
            Map::class, MutableMap::class -> {
                val keyType = type.arguments.getOrNull(0)?.type
                require(keyType?.classifier == String::class) {
                    "Map parameter keys must be String for automatic binding: $type"
                }
                val valueType =
                    type.arguments.getOrNull(1)?.type
                        ?: throw IllegalArgumentException(
                            "Map parameter type must declare a value type: $type",
                        )
                Schema(
                    type = "object",
                    additionalProperties = AdditionalProperties.SchemaValue(buildTypeSchema(valueType)),
                )
            }
            else -> buildDescriptorSchema(serializer(type).descriptor)
        }

    return baseSchema.copy(nullable = type.isMarkedNullable)
}

private fun buildDescriptorSchema(descriptor: SerialDescriptor): Schema {
    val baseSchema =
        when (descriptor.kind) {
            PrimitiveKind.INT, PrimitiveKind.LONG -> Schema(type = "integer")
            PrimitiveKind.STRING -> Schema(type = "string")
            PrimitiveKind.BOOLEAN -> Schema(type = "boolean")
            PrimitiveKind.DOUBLE, PrimitiveKind.FLOAT -> Schema(type = "number")
            SerialKind.ENUM ->
                Schema(
                    type = "string",
                    enum = List(descriptor.elementsCount) { index -> descriptor.getElementName(index) },
                )
            StructureKind.LIST ->
                Schema(
                    type = "array",
                    items = buildDescriptorSchema(descriptor.getElementDescriptor(0)),
                )
            StructureKind.MAP -> {
                val keyDescriptor = descriptor.getElementDescriptor(0)
                require(keyDescriptor.kind == PrimitiveKind.STRING) {
                    "Map parameter keys must be String for automatic binding: ${descriptor.serialName}"
                }
                Schema(
                    type = "object",
                    additionalProperties =
                        AdditionalProperties.SchemaValue(
                            buildDescriptorSchema(descriptor.getElementDescriptor(1)),
                        ),
                )
            }
            StructureKind.CLASS, StructureKind.OBJECT ->
                Schema(
                    type = "object",
                    properties =
                        buildMap {
                            for (index in 0 until descriptor.elementsCount) {
                                put(
                                    descriptor.getElementName(index),
                                    buildDescriptorSchema(descriptor.getElementDescriptor(index)),
                                )
                            }
                        },
                    required =
                        buildList {
                            for (index in 0 until descriptor.elementsCount) {
                                if (!descriptor.isElementOptional(index)) {
                                    add(descriptor.getElementName(index))
                                }
                            }
                        },
                )
            else -> throw IllegalArgumentException("Unsupported parameter type: ${descriptor.serialName}")
        }

    return baseSchema.copy(nullable = descriptor.isNullable)
}

private fun buildParameterSchema(
    type: KType,
    description: String,
): Schema =
    buildTypeSchema(type).copy(
        description = description,
        nullable = type.isMarkedNullable,
    )

private fun requiredParameterNames(function: KFunction<*>): List<String> =
    function
        .valueParameters
        .filterNot { it.isOptional }
        .map { parameter ->
            parameter.name ?: error("Unnamed parameter in function '${function.name}'.")
        }

private fun parameterSchemas(function: KFunction<*>): Map<String, Schema> =
    function.valueParameters.associate { param ->
        val paramAnnotation =
            param.findAnnotation<GeminiParameter>()
                ?: throw IllegalArgumentException(
                    "Parameter '${param.name}' must be annotated with @GeminiParameter",
                )
        val paramName = param.name ?: error("Unnamed parameter in function '${function.name}'.")
        paramName to buildParameterSchema(param.type, paramAnnotation.description)
    }

/**
 * Handles build function declaration.
 *
 * @param function The function.
 */
actual fun buildFunctionDeclaration(function: KFunction<*>): FunctionDeclaration {
    /**
     * Holds the function annotation.
     */
    val functionAnnotation =
        function.findAnnotation<GeminiFunction>()
            ?: throw IllegalArgumentException("Function must be annotated with @GeminiFunction")

    return FunctionDeclaration(
        name = function.name,
        description = functionAnnotation.description,
        parameters =
            Schema(
                type = "object",
                properties = parameterSchemas(function),
                required = requiredParameterNames(function),
            ),
    )
}
