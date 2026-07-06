package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.interaction.Interaction
import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import io.github.ugaikit.gemini4kt.interaction.InteractionStatus
import io.github.ugaikit.gemini4kt.interaction.InteractionStep
import io.github.ugaikit.gemini4kt.interaction.InteractionThoughtSummary
import kotlinx.serialization.json.JsonPrimitive
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertTrue

class InteractionSamplesCoverageTest {
    @Test
    fun printInteractionResultCoversContentAndStepBranches() {
        val interaction =
            Interaction(
                id = "inter_branch",
                status = InteractionStatus.COMPLETED,
                model = "gemini-2.5-flash",
                outputTextRaw = null,
                outputs =
                    arrayOf(
                        InteractionContent(type = "text", text = "hello"),
                        InteractionContent(type = "image", data = "image-data"),
                        InteractionContent(type = "function_call", name = "get_weather"),
                        InteractionContent(
                            type = "thought",
                            summary =
                                InteractionThoughtSummary(
                                    content = InteractionContent(type = "text", text = "thinking"),
                                ),
                        ),
                        InteractionContent(type = "code_execution_result", result = JsonPrimitive("ok")),
                        InteractionContent(type = "unknown"),
                    ),
                steps =
                    arrayOf(
                        InteractionStep(type = "user_input", content = null),
                        InteractionStep(
                            type = "model_output",
                            content = arrayOf(InteractionContent(type = "text", text = "hello")),
                        ),
                        InteractionStep(
                            type = "user_input",
                            content =
                                arrayOf(
                                    InteractionContent(type = "text", text = "alpha"),
                                    InteractionContent(type = "image", data = "payload"),
                                    InteractionContent(type = "function_call", name = "lookup"),
                                    InteractionContent(type = "mystery"),
                                ),
                        ),
                    ),
            )

        val output =
            captureStdout {
                val method =
                    InteractionSamples::class.java.getDeclaredMethod("printInteractionResult", Interaction::class.java)
                method.isAccessible = true
                method.invoke(InteractionSamples, interaction)
            }

        assertTrue(output.contains("Output text: hello"))
        assertTrue(output.contains("Text length: 5"))
        assertTrue(output.contains("Image payload present: true"))
        assertTrue(output.contains("Function call name: get_weather"))
        assertTrue(output.contains("Thought summary present: true"))
        assertTrue(output.contains("Code result present: true"))
        assertTrue(output.contains("Content type logged without payload details."))
        assertTrue(output.contains("Step[0] type: user_input"))
        assertTrue(output.contains("No step content returned."))
        assertTrue(output.contains("Step[1] type: model_output"))
        assertTrue(output.contains("Content[0]: text=alpha"))
        assertTrue(output.contains("Content[1]: data=<image payload>"))
        assertTrue(output.contains("Content[2]: name=lookup"))
        assertTrue(output.contains("Content[3]: mystery"))
    }

    @Test
    fun printInteractionResultHandlesMissingOutputsAndSteps() {
        val interaction =
            Interaction(
                id = "inter_empty",
                status = InteractionStatus.COMPLETED,
            )

        val output =
            captureStdout {
                val method =
                    InteractionSamples::class.java.getDeclaredMethod("printInteractionResult", Interaction::class.java)
                method.isAccessible = true
                method.invoke(InteractionSamples, interaction)
            }

        assertTrue(output.contains("No generated content items returned."))
        assertTrue(output.contains("No interaction steps returned."))
    }

    private fun captureStdout(block: () -> Unit): String {
        val original = System.out
        val buffer = ByteArrayOutputStream()
        val printStream = PrintStream(buffer, true, Charsets.UTF_8.name())
        return try {
            System.setOut(printStream)
            block()
            printStream.flush()
            buffer.toString(Charsets.UTF_8.name())
        } finally {
            System.setOut(original)
        }
    }
}
