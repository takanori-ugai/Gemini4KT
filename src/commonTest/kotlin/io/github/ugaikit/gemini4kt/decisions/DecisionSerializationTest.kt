package io.github.ugaikit.gemini4kt.decisions

import io.github.ugaikit.gemini4kt.interaction.InteractionContent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecisionSerializationTest {
    @Test
    fun allSupportedInputFormsSerializeWithoutAWrappingObject() {
        val blocks =
            listOf(
                InteractionContent(type = "text", text = "Evaluate this."),
                InteractionContent(type = "image", uri = "files/image", mimeType = "image/png"),
                InteractionContent(type = "audio", data = "AQID", mimeType = "audio/wav"),
                InteractionContent(type = "video", uri = "https://example.com/clip.mp4", mimeType = "video/mp4"),
                InteractionContent(type = "document", data = "AQID", mimeType = "application/pdf"),
            )
        val inputs = listOf(DecisionInput("Hello"), DecisionInput(blocks.first()), DecisionInput(blocks))
        for (input in inputs) {
            val encoded = Json.encodeToString(input)
            assertEquals(input.value, Json.parseToJsonElement(encoded))
            assertEquals(input, Json.decodeFromString<DecisionInput>(encoded))
        }
        assertEquals(5, Json.parseToJsonElement(Json.encodeToString(inputs.last())).jsonArray.size)
    }

    @Test
    fun instructionsAcceptStringsAndTextBlocksWithOptionalBinaryCriteria() {
        val criteria = NoulCriteria(trueDescription = "Broken", falseDescription = "Not broken")
        val instructions =
            DecisionInput(listOf(InteractionContent(type = "text", text = "Does it report damage?")))
        val request =
            CreateDecisionRequest("Broken glass", mapOf("damage" to DecisionQuestion.Noul(instructions, criteria)))
        val encoded = Json.encodeToString(request)
        val fields =
            Json
                .parseToJsonElement(encoded)
                .jsonObject
                .getValue("questions")
                .jsonObject
                .getValue("damage")
                .jsonObject
        val instruction =
            fields
                .getValue("instructions")
                .jsonArray
                .single()
                .jsonObject
        assertEquals(JsonPrimitive("text"), instruction["type"])
        assertEquals(JsonPrimitive("Broken"), fields.getValue("criteria").jsonObject["true_description"])
        assertEquals(JsonPrimitive("Not broken"), fields.getValue("criteria").jsonObject["false_description"])
        assertEquals(request, Json.decodeFromString<CreateDecisionRequest>(encoded))
    }

    @Test
    fun typedAnswersCanBePersistedAndRestoredWithoutLosingProbabilities() {
        val response =
            DecisionResponse(
                answers =
                    mapOf(
                        "binary" to DecisionAnswer.Noul(noul = 0.03, confidence = 0.97),
                        "category" to
                            DecisionAnswer.Choice(
                                choice = "billing",
                                probabilities = mapOf("billing" to 0.75, "other" to 0.25),
                                confidence = 0.75,
                            ),
                        "rubric" to
                            DecisionAnswer.Score(
                                score = 0.8,
                                probabilities = mapOf("0" to 0.2, "1" to 0.8),
                                legend = mapOf("0" to "Low", "1" to "High"),
                                confidence = 0.8,
                            ),
                    ),
            )
        val encoded = Json.encodeToString(response)
        assertEquals(response, Json.decodeFromString<DecisionResponse>(encoded))
    }

    @Test
    fun invalidQuestionCountsAndKeysAreRejected() {
        val question = DecisionQuestion.Noul("Is it relevant?")
        assertFailsWith<IllegalArgumentException> { CreateDecisionRequest("Hello", emptyMap()) }
        assertFailsWith<IllegalArgumentException> {
            CreateDecisionRequest("Hello", (0..1000).associate { "q$it" to question })
        }
        for (key in listOf("", "bad key", "a".repeat(65), "日本語")) {
            assertFailsWith<IllegalArgumentException> { CreateDecisionRequest("Hello", mapOf(key to question)) }
        }
        assertFailsWith<IllegalArgumentException> {
            CreateDecisionRequest("Hello", mapOf("q" to question), model = " ")
        }
    }

    @Test
    fun criteriaBoundsAndTextOnlyInstructionsAreEnforced() {
        assertFailsWith<IllegalArgumentException> { DecisionQuestion.Choice("Pick", mapOf("a" to "")) }
        assertFailsWith<IllegalArgumentException> {
            DecisionQuestion.Choice("Pick", (0..255).associate { "option$it" to "" })
        }
        assertFailsWith<IllegalArgumentException> { DecisionQuestion.Score("Rate", listOf("One")) }
        assertFailsWith<IllegalArgumentException> { DecisionQuestion.Score("Rate", listOf("One", "")) }
        assertFailsWith<IllegalArgumentException> { DecisionQuestion.Score("Rate", List(256) { "Level $it" }) }
        val image = DecisionInput(InteractionContent(type = "image", data = "AQID", mimeType = "image/png"))
        assertFailsWith<IllegalArgumentException> { DecisionQuestion.Noul(image) }
        assertFailsWith<IllegalArgumentException> { DecisionQuestion.Choice(image, mapOf("a" to "", "b" to "")) }
        assertFailsWith<IllegalArgumentException> { DecisionQuestion.Score(image, listOf("Low", "High")) }
        assertFailsWith<IllegalArgumentException> {
            DecisionInput(InteractionContent(type = "function_call", name = "tool"))
        }
        assertFailsWith<IllegalArgumentException> { DecisionInput(emptyList()) }
        assertFailsWith<IllegalArgumentException> { Json.decodeFromString<DecisionInput>("42") }
    }
}
