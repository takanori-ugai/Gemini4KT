package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

/**
 * Represents the batch sample runner.
 */
object BatchSampleRunner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            BatchSample.run()
        }
}
