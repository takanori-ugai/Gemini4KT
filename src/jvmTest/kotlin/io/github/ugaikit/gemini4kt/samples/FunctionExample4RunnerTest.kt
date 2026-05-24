package io.github.ugaikit.gemini4kt.samples

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class FunctionExample4RunnerTest {
    @Test
    fun mainDelegatesToFunctionExample4Run() =
        runTest {
            mockkObject(FunctionExample4)
            try {
                coEvery { FunctionExample4.run(any()) } returns Unit

                FunctionExample4Runner.main(emptyArray())

                coVerify(exactly = 1) { FunctionExample4.run(any()) }
            } finally {
                unmockkObject(FunctionExample4)
            }
        }
}
