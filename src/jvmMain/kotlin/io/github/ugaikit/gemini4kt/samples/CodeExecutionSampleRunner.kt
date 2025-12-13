package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking

object CodeExecutionSampleRunner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            CodeExecutionSample.run()
        }
}
