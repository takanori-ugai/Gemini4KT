package io.github.ugaikit.gemini4kt

import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters

@Target(AnnotationTarget.FUNCTION)
annotation class GeminiFunction(
    val description: String,
)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class GeminiParameter(
    val description: String,
)

private fun getTypeName(type: kotlin.reflect.KClassifier): String =
    when (type) {
        Int::class -> "integer"
        String::class -> "string"
        Boolean::class -> "boolean"
        Double::class -> "number"
        Float::class -> "number"
        Long::class -> "integer"
        else -> throw IllegalArgumentException("Unsupported parameter type: $type")
    }

fun buildFunctionDeclaration(function: KFunction<*>): FunctionDeclaration {
    val functionAnnotation =
        function.findAnnotation<GeminiFunction>()
            ?: throw IllegalArgumentException("Function must be annotated with @GeminiFunction")

    val properties =
        function.valueParameters.associate { param ->
            val paramAnnotation =
                param.findAnnotation<GeminiParameter>()
                    ?: throw IllegalArgumentException(
                        "Parameter '${param.name}' must be annotated with @GeminiParameter",
                    )
            val typeName = getTypeName(param.type.classifier!!)
            param.name!! to Schema(type = typeName, description = paramAnnotation.description)
        }

    return FunctionDeclaration(
        name = function.name,
        description = functionAnnotation.description,
        parameters =
            Schema(
                type = "object",
                properties = properties,
                required = function.valueParameters.map { it.name!! },
            ),
    )
}
