package io.github.ugaikit.gemini4kt

/**
 * Test helpers.
 */
object TestUtils {
    /**
     * Captures stdout for the duration of [block] and returns it as a string.
     */
    inline fun captureStdout(block: () -> Unit): String {
        val original = System.out
        val buffer = java.io.ByteArrayOutputStream()
        System.setOut(java.io.PrintStream(buffer))
        return try {
            block()
            buffer.toString()
        } finally {
            System.setOut(original)
        }
    }
}
