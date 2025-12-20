package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.getApiKey
import kotlinx.coroutines.runBlocking

/**
 * Represents the url context sample runner.
 */
object UrlContextSampleRunner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            UrlContextSample.run(Gemini(getApiKey()))
        }
    }
}
