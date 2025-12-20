package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.getApiKey
import kotlinx.coroutines.runBlocking

/**
 * Represents the stream generate content sample runner.
 */
object StreamGenerateContentSampleRunner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            StreamGenerateContentSample.run(Gemini(getApiKey()))
        }
    }
}
