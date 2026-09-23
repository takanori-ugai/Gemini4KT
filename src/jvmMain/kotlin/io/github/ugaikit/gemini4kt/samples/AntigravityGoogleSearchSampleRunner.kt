package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

/**
 * Runs the Antigravity Google Search sample on the JVM.
 */
object AntigravityGoogleSearchSampleRunner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            AntigravityGoogleSearchSample.run()
        }
}
