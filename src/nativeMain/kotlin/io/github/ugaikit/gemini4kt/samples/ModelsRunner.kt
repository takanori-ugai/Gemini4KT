package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

/**
 * Entry point for running the Models sample.
 *
 * This function uses Kotlin coroutines to run a blocking operation
 * that lists all available models using the `Models` object.
 */
fun main() =
    runBlocking {
        Models.listModels()
    }
