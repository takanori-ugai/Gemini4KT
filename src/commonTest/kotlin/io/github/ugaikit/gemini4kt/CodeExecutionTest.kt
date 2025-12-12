package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CodeExecutionTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            prettyPrint = false
        }

    @Test
    fun `test tool with code execution serialization`() {
        val tool =
            tool {
                codeExecution()
            }
        val encoded = json.encodeToString(tool)
        // code_execution should be an empty object
        // functionDeclarations is empty list by default in builder and serialized because it's not null
        val expected = """{"functionDeclarations":[],"code_execution":{}}"""
        assertEquals(expected, encoded)
    }

    @Test
    fun `test part with executable code serialization`() {
        val part =
            part {
                executableCode {
                    language = "PYTHON"
                    code = "print('hello')"
                }
            }
        val encoded = json.encodeToString(part)
        val expected = """{"executableCode":{"language":"PYTHON","code":"print('hello')"}}"""
        assertEquals(expected, encoded)
    }

    @Test
    fun `test part with code execution result serialization`() {
        val part =
            part {
                codeExecutionResult {
                    outcome = "OUTCOME_OK"
                    output = "hello"
                }
            }
        val encoded = json.encodeToString(part)
        val expected = """{"codeExecutionResult":{"outcome":"OUTCOME_OK","output":"hello"}}"""
        assertEquals(expected, encoded)
    }

    @Test
    fun `test part with text and code execution result serialization`() {
        // Just checking multiple fields
        val part =
            part {
                text { "Result:" }
                codeExecutionResult {
                    outcome = "OUTCOME_OK"
                    output = "hello"
                }
            }
        val encoded = json.encodeToString(part)
        // Order of keys might vary depending on serialization, but kotlin serialization usually preserves definition order
        // defined in class: text, inlineData, functionCall, functionResponse, fileData, executableCode, codeExecutionResult
        val expected = """{"text":"Result:","codeExecutionResult":{"outcome":"OUTCOME_OK","output":"hello"}}"""
        assertEquals(expected, encoded)
    }
}
