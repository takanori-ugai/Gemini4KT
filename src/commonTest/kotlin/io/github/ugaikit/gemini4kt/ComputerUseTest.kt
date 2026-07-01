package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Represents the computer use test.
 */
class ComputerUseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun computerUseSupportsValueEquality() {
        val first =
            ComputerUse(
                environment = Environment.ENVIRONMENT_BROWSER,
                excludedPredefinedFunctions = listOf("CLICK", "TYPE"),
            )
        val second =
            ComputerUse(
                environment = Environment.ENVIRONMENT_BROWSER,
                excludedPredefinedFunctions = listOf("CLICK", "TYPE"),
            )

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun computerUseSerializesWithExcludedPredefinedFunctions() {
        val computerUse =
            ComputerUse(
                environment = Environment.ENVIRONMENT_BROWSER,
                excludedPredefinedFunctions = listOf("CLICK", "TYPE"),
            )

        val encoded = json.encodeToString(computerUse)
        val decoded = json.decodeFromString<ComputerUse>(encoded)

        assertEquals(computerUse, decoded)
        assertEquals(listOf("CLICK", "TYPE"), decoded.excludedPredefinedFunctions)
    }
}
