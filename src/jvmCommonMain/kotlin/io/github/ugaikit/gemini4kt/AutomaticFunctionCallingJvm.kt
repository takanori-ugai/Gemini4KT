package io.github.ugaikit.gemini4kt

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

/**
 * Creates Gemini tool declarations from annotated Kotlin functions.
 */
fun buildFunctionTools(vararg functions: KFunction<*>): Array<Tool> {
    require(functions.isNotEmpty()) { "At least one function is required." }
    val declarations = functions.map(::buildFunctionDeclaration).toTypedArray()
    return arrayOf(Tool(functionDeclarations = declarations))
}

/**
 * Builds automatic function-calling metadata on JVM/Android.
 */
actual fun buildAutomaticFunctionBinding(functions: Array<out KFunction<*>>): AutomaticFunctionBinding {
    require(functions.isNotEmpty()) { "At least one function is required." }
    val tools = buildFunctionTools(*functions)
    val handlers =
        functions.associate { function ->
            function.name to
                suspend { functionCall: FunctionCall ->
                    invokeBoundFunction(function, functionCall)
                }
        }
    return AutomaticFunctionBinding(tools = tools, handlers = handlers)
}

private suspend fun invokeBoundFunction(
    function: KFunction<*>,
    functionCall: FunctionCall,
): FunctionResponse {
    function.isAccessible = true
    require(function.name == functionCall.name) {
        "Function name mismatch. Expected '${function.name}' but got '${functionCall.name}'."
    }
    require(function.instanceParameter == null) {
        "Only top-level or bound functions are supported: '${function.name}'."
    }

    val arguments = mutableMapOf<KParameter, Any?>()
    function.valueParameters.forEach { parameter ->
        val parameterName = parameter.name ?: error("Unnamed parameter in function '${function.name}'.")
        val rawArg = functionCall.args[parameterName]

        if (rawArg == null) {
            when {
                parameter.isOptional -> Unit
                parameter.type.isMarkedNullable -> arguments[parameter] = null
                else ->
                    throw IllegalArgumentException(
                        "Missing required argument '$parameterName' for function '${function.name}'.",
                    )
            }
        } else {
            arguments[parameter] = jsonElementToKotlinValue(rawArg, parameter.type)
        }
    }

    val result = function.callSuspendBy(arguments)
    return when (result) {
        is FunctionResponse -> result
        is JsonObject -> FunctionResponse(name = function.name, response = result)
        else ->
            FunctionResponse(
                name = function.name,
                response =
                    buildJsonObject {
                        put("result", anyToJsonElement(result))
                    },
            )
    }
}

private fun jsonElementToKotlinValue(
    element: JsonElement,
    type: KType,
): Any? {
    if (element is JsonNull) {
        if (!type.isMarkedNullable) {
            throw IllegalArgumentException("Non-null parameter received null for type $type")
        }
        return null
    }

    return when (type.classifier) {
        String::class -> element.jsonPrimitive.content
        Int::class -> element.jsonPrimitive.int
        Long::class -> element.jsonPrimitive.long
        Double::class -> element.jsonPrimitive.double
        Float::class -> element.jsonPrimitive.float
        Boolean::class -> element.jsonPrimitive.boolean
        JsonElement::class -> element
        JsonObject::class -> element as? JsonObject ?: error("Expected JsonObject but got $element")
        JsonArray::class -> element as? JsonArray ?: error("Expected JsonArray but got $element")
        else -> throw IllegalArgumentException("Unsupported parameter type for automatic binding: $type")
    }
}

private fun anyToJsonElement(value: Any?): JsonElement =
    when (value) {
        null -> JsonNull
        is JsonElement -> value
        is String -> JsonPrimitive(value)
        is Int -> JsonPrimitive(value)
        is Long -> JsonPrimitive(value)
        is Double -> JsonPrimitive(value)
        is Float -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is Map<*, *> ->
            buildJsonObject {
                value.forEach { (k, v) ->
                    require(k is String) { "Only String map keys are supported in function responses." }
                    put(k, anyToJsonElement(v))
                }
            }
        is Iterable<*> ->
            buildJsonArray {
                value.forEach { add(anyToJsonElement(it)) }
            }
        is Array<*> ->
            buildJsonArray {
                value.forEach { add(anyToJsonElement(it)) }
            }
        else -> JsonPrimitive(value.toString())
    }
