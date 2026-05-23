package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringListTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun equalsCoversAllBranches() {
        val value = StringList(values = arrayOf("a", "b"))
        val sameContent = StringList(values = arrayOf("a", "b"))
        val differentContent = StringList(values = arrayOf("a", "c"))

        assertTrue(value == value)
        assertTrue(value == sameContent)
        assertFalse(value.equals(null))
        assertFalse(value.equals("not-a-string-list"))
        assertFalse(value == differentContent)
    }

    @Test
    fun hashCodeUsesArrayContent() {
        val left = StringList(values = arrayOf("x", "y"))
        val right = StringList(values = arrayOf("x", "y"))

        assertEquals(left.hashCode(), right.hashCode())
    }

    @Test
    fun serializationRoundTrip() {
        val original = StringList(values = arrayOf("hello", "world"))

        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<StringList>(encoded)

        assertEquals(original, decoded)
    }
}
