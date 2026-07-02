package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThinkingConfigTest {
    private val json = Json { encodeDefaults = false }

    @Test
    fun defaultThinkingBudgetIsNotForcedIntoJson() {
        val encoded = json.encodeToString(ThinkingConfig())

        assertFalse(encoded.contains("thinking_budget"))
    }

    @Test
    fun explicitThinkingBudgetIsSerialized() {
        val encoded = json.encodeToString(ThinkingConfig(thinkingBudget = 256))

        assertTrue(encoded.contains("\"thinking_budget\":256"))
    }
}
