package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

/**
 * Represents the function example1 runner.
 */
object FunctionExample1Runner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            FunctionExample1.run()
        }
}
