package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking
import java.io.File

object FileSearchSampleRunner {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            val file = File("sample.txt")
            if (!file.exists()) {
                file.writeText("The quick brown fox jumps over the lazy dog.")
            }
            FileSearchSample.run(file.path)
        }
}
