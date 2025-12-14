package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

fun main() =
    runBlocking {
        Models.listModels()
    }
