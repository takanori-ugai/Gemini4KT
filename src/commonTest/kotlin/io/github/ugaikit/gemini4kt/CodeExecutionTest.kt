package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Represents the code execution test.
 */
class CodeExecutionTest {
    /**
     * Holds the json.
     */
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            prettyPrint = false
        }

    /**
     * Tests test tool with code execution serialization.
     */
    @Test
    fun testToolWithCodeExecutionSerialization() {
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

    /**
     * Tests test part with executable code serialization.
     */
    @Test
    fun testPartWithExecutableCodeSerialization() {
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

    /**
     * Tests test part with code execution result serialization.
     */
    @Test
    fun testPartWithCodeExecutionResultSerialization() {
        val part =
            part {
                codeExecutionResult {
                    outcome { "OUTCOME_OK" }
                    output { "hello" }
                }
            }
        val encoded = json.encodeToString(part)
        val expected = """{"codeExecutionResult":{"outcome":"OUTCOME_OK","output":"hello"}}"""
        assertEquals(expected, encoded)
    }

    /**
     * Tests code execution result serialization with image only.
     */
    @Test
    fun testPartWithCodeExecutionResultImageSerialization() {
        val part =
            part {
                codeExecutionResult {
                    outcome { "OUTCOME_OK" }
                    image {
                        mimeType { "image/png" }
                        data { "base64data" }
                    }
                }
            }
        val encoded = json.encodeToString(part)
        val expected =
            "{\"codeExecutionResult\":{\"outcome\":\"OUTCOME_OK\",\"image\":{\"mimeType\":\"image/png\"," +
                "\"data\":\"base64data\"}}}"
        assertEquals(expected, encoded)
    }

    /**
     * Tests code execution result serialization with image and output.
     */
    @Test
    fun testPartWithCodeExecutionResultImageAndOutputSerialization() {
        val part =
            part {
                codeExecutionResult {
                    outcome { "OUTCOME_OK" }
                    output { "some text" }
                    image {
                        mimeType { "image/jpeg" }
                        data { "imgdata" }
                    }
                }
            }
        val encoded = json.encodeToString(part)
        val expected =
            "{\"codeExecutionResult\":{\"outcome\":\"OUTCOME_OK\",\"output\":\"some text\"," +
                "\"image\":{\"mimeType\":\"image/jpeg\",\"data\":\"imgdata\"}}}"
        assertEquals(expected, encoded)
    }

    /**
     * Tests test part with text and code execution result serialization.
     */
    @Test
    fun testPartWithTextAndCodeExecutionResultSerialization() {
        // Just checking multiple fields
        val part =
            part {
                text { "Result:" }
                codeExecutionResult {
                    outcome { "OUTCOME_OK" }
                    output { "hello" }
                }
            }
        val encoded = json.encodeToString(part)
        // Order of keys might vary depending on serialization, but kotlin serialization usually
        // preserves definition order defined in class: text, inlineData, functionCall,
        // functionResponse, fileData, executableCode, codeExecutionResult
        val expected = """{"text":"Result:","codeExecutionResult":{"outcome":"OUTCOME_OK","output":"hello"}}"""
        assertEquals(expected, encoded)
    }
}
