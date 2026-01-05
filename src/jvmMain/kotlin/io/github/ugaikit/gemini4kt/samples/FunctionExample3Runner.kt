package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

/**
 * Represents the function example3 runner.
 */
object FunctionExample3Runner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            FunctionExample3.run()
        }
}
