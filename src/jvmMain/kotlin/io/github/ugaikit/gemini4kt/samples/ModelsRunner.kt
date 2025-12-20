package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

/**
 * Handles main.
 */
fun main() =
    runBlocking {
        Models.listModels()
    }
