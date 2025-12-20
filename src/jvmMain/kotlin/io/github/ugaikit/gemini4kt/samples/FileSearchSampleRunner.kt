package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Represents the file search sample runner.
 */
object FileSearchSampleRunner {
    /**
     * Handles main.
     *
     * @param args The args.
     */
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
