package io.github.ugaikit.gemini4kt

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.Test

class UsageMetadataTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test serialization`() {
        val usageMetadata =
            UsageMetadata(
                promptTokenCount = 31,
                candidatesTokenCount = 323,
                totalTokenCount = 1182,
                promptTokensDetails =
                    listOf(
                        ModalityTokenCount(Modality.TEXT, 31),
                    ),
                toolUsePromptTokenCount = 712,
                toolUsePromptTokensDetails =
                    listOf(
                        ModalityTokenCount(Modality.TEXT, 712),
                    ),
            )
        val expectedJson =
            """
            {"promptTokenCount":31,"candidatesTokenCount":323,"totalTokenCount":1182,
            "promptTokensDetails":[{"modality":"TEXT","tokenCount":31}],"toolUsePromptTokenCount":712,
            "toolUsePromptTokensDetails":[{"modality":"TEXT","tokenCount":712}]}
            """.replace("\n", "").replace(" ", "")
        assertEquals(expectedJson, json.encodeToString(usageMetadata))
    }

    @Test
    fun `test deserialization`() {
        val jsonString =
            """
            {
                "promptTokenCount": 31,
                "candidatesTokenCount": 323,
                "totalTokenCount": 1182,
                "promptTokensDetails": [
                  {
                    "modality": "TEXT",
                    "tokenCount": 31
                  }
                ],
                "toolUsePromptTokenCount": 712,
                "toolUsePromptTokensDetails": [
                  {
                    "modality": "TEXT",
                    "tokenCount": 712
                  }
                ]
            }
            """.trimIndent()

        val usageMetadata = json.decodeFromString<UsageMetadata>(jsonString)

        assertEquals(31, usageMetadata.promptTokenCount)
        assertEquals(323, usageMetadata.candidatesTokenCount)
        assertEquals(1182, usageMetadata.totalTokenCount)
        assertEquals(1, usageMetadata.promptTokensDetails?.size)
        assertEquals(Modality.TEXT, usageMetadata.promptTokensDetails?.get(0)?.modality)
        assertEquals(31, usageMetadata.promptTokensDetails?.get(0)?.tokenCount)
        assertEquals(712, usageMetadata.toolUsePromptTokenCount)
        assertEquals(1, usageMetadata.toolUsePromptTokensDetails?.size)
        assertEquals(Modality.TEXT, usageMetadata.toolUsePromptTokensDetails?.get(0)?.modality)
        assertEquals(712, usageMetadata.toolUsePromptTokensDetails?.get(0)?.tokenCount)
    }

    @Test
    fun `test deserialization with missing fields`() {
        val jsonString =
            """
            {
                "totalTokenCount": 1182
            }
            """.trimIndent()

        val usageMetadata = json.decodeFromString<UsageMetadata>(jsonString)

        assertEquals(null, usageMetadata.promptTokenCount)
        assertEquals(null, usageMetadata.candidatesTokenCount)
        assertEquals(1182, usageMetadata.totalTokenCount)
        assertEquals(null, usageMetadata.promptTokensDetails)
        assertEquals(null, usageMetadata.toolUsePromptTokenCount)
        assertEquals(null, usageMetadata.toolUsePromptTokensDetails)
    }
}
