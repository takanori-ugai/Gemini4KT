package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

object FunctionExample1Runner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            FunctionExample1.run()
        }
}
