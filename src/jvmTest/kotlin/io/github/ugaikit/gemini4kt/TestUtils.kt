package io.github.ugaikit.gemini4kt

/**
 * Test helpers.
 */
object TestUtils {
    private val stdoutMutex = kotlinx.coroutines.sync.Mutex()

    /**
     * Captures stdout for the duration of [block] and returns it as a string.
     */
    suspend fun captureStdout(block: suspend () -> Unit): String {
        stdoutMutex.lock()
        val original = System.out
        val buffer = java.io.ByteArrayOutputStream()
        System.setOut(java.io.PrintStream(buffer))
        return try {
            block()
            buffer.toString()
        } finally {
            System.setOut(original)
            stdoutMutex.unlock()
        }
    }
}
