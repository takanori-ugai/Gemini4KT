package io.github.ugaikit.gemini4kt

import io.github.ugaikit.gemini4kt.interaction.CreateInteractionRequest
import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.interaction.InteractionInput
import io.github.ugaikit.gemini4kt.interaction.InteractionStep
import io.github.ugaikit.gemini4kt.interaction.InteractionTool
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.reflect.KFunction

/**
 * Creates an interaction and automatically runs annotated Kotlin functions returned as tool calls.
 *
 * @param request Initial interaction request.
 * @param functions Top-level or bound functions annotated with [GeminiFunction].
 * @param maxIterations Maximum number of tool-call rounds.
 */
suspend fun GeminiAI.createInteraction(
    request: CreateInteractionRequest,
    vararg functions: KFunction<*>,
    maxIterations: Int = 8,
): Interaction {
    require(functions.isNotEmpty()) { "At least one function is required." }
    require(maxIterations > 0) { "maxIterations must be greater than 0." }

    val binding = buildAutomaticFunctionBinding(functions)
    val functionTools = binding.tools
        .flatMap { tool -> tool.functionDeclarations.orEmpty().toList() }
        .map { declaration ->
            InteractionTool(
                type = "function",
                name = declaration.name,
                description = declaration.description,
                parameters = Json.parseToJsonElement(Json.encodeToString(declaration.parameters)),
            )
        }
    var nextRequest = request.copy(tools = (request.tools.orEmpty().toList() + functionTools).toTypedArray())

    repeat(maxIterations) {
        val interaction = createInteraction(nextRequest)
        val functionCalls = interaction.steps.orEmpty().filter { it.type == "function_call" }
        if (functionCalls.isEmpty()) return interaction

        val results = functionCalls.map { call ->
            val functionName = call.name ?: error("Function call did not include a name: $call")
            val callId = call.id ?: call.callId ?: error("Function call did not include an id: $call")
            val arguments = call.arguments?.jsonObject ?: error("Function call did not include object arguments: $call")
            val response =
                (binding.handlers[functionName]
                    ?: error("No function handler registered for '$functionName'."))(
                    FunctionCall(name = functionName, args = arguments),
                )
            InteractionStep(
                type = "function_result",
                name = functionName,
                callId = callId,
                result = response.response,
            )
        }
        nextRequest = nextRequest.copy(
            input = InteractionInput.StepList(results.toTypedArray()),
            previousInteractionId = interaction.id,
        )
    }

    error("Automatic interaction function calling exceeded maxIterations=$maxIterations.")
}
