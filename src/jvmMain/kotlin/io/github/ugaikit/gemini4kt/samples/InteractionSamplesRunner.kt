package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

/**
 * Represents the interaction samples runner.
 */
object InteractionSamplesRunner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            try {
                InteractionSamples.runSimple()
            } catch (e: Exception) {
                println("Simple Request failed: ${e.message}")
            }
            try {
                InteractionSamples.runMultiTurn()
            } catch (e: Exception) {
                println("Multi-turn failed: ${e.message}")
            }
            try {
                InteractionSamples.runImageInput()
            } catch (e: Exception) {
                println("Image Input failed: ${e.message}")
            }
            try {
                InteractionSamples.runFunctionCalling()
            } catch (e: Exception) {
                println("Function Calling failed: ${e.message}")
            }
            try {
                InteractionSamples.runDeepResearch()
            } catch (e: Exception) {
                println("Deep Research failed: ${e.message}")
            }
        }
    }
}
