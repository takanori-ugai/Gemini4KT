package io.github.ugaikit.gemini4kt.samples

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Handles main.
 */
fun main() {
    GlobalScope.launch {
        Models.listModels()
    }
}
