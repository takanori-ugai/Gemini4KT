package io.github.ugaikit.gemini4kt.samples

import io.github.ugaikit.gemini4kt.Content
import io.github.ugaikit.gemini4kt.Gemini
import io.github.ugaikit.gemini4kt.GenerateContentRequest
import io.github.ugaikit.gemini4kt.Part
import io.github.ugaikit.gemini4kt.filesearch.ChunkingConfig
import io.github.ugaikit.gemini4kt.filesearch.FileSearch
import io.github.ugaikit.gemini4kt.filesearch.FileSearchStore
import io.github.ugaikit.gemini4kt.filesearch.UploadFileSearchStoreRequest
import io.github.ugaikit.gemini4kt.filesearch.WhiteSpaceConfig
import io.github.ugaikit.gemini4kt.getApiKey
import io.github.ugaikit.gemini4kt.tool
import kotlinx.coroutines.delay
import kotlinx.io.files.Path

/**
 * Represents the file search sample.
 */
object FileSearchSample {
    /**
     * Handles run.
     *
     * @param filePath The file path.
     * @param geminiInstance The gemini instance.
     * @param fileSearchInstance The file search instance.
     */
    suspend fun run(
        filePath: String,
        geminiInstance: Gemini? = null,
        fileSearchInstance: FileSearch? = null,
    ) {
        val apiKey = getApiKey()
        val ownsGemini = geminiInstance == null
        val ownsFileSearch = fileSearchInstance == null
        val fileSearch = fileSearchInstance ?: FileSearch(apiKey)
        val gemini = geminiInstance ?: Gemini(apiKey)
        var storeName: String? = null

        try {
            // 1. Create FileSearchStore
            val store =
                fileSearch.createFileSearchStore(
                    FileSearchStore(displayName = "your-fileSearchStore-name"),
                )
            storeName = store.name
            println("Created FileSearchStore: ${store.name}")

            // 2. Upload file
            val resolvedStoreName = storeName ?: return
            uploadFileToStore(fileSearch, resolvedStoreName, filePath)

            // 3. Generate Content
            val generateContentRequest =
                GenerateContentRequest(
                    contents = arrayOf(Content(parts = arrayOf(Part(text = "What does the fox do?")))),
                    tools =
                        arrayOf(
                            tool {
                                fileSearch {
                                    fileSearchStoreName(resolvedStoreName)
                                }
                            },
                        ),
                )

            val response =
                gemini.generateContent(
                    model = "gemma-4-31b-it",
                    inputJson = generateContentRequest,
                )

            println(
                response.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text,
            )
        } finally {
            // Clean up
            try {
                if (storeName != null) {
                    fileSearch.deleteFileSearchStore(storeName, force = true)
                }
            } finally {
                if (ownsGemini) {
                    gemini.close()
                }
                if (ownsFileSearch) {
                    fileSearch.close()
                }
            }
        }
    }

    /**
     * Handles upload file to store.
     *
     * @param fileSearch The file search.
     * @param storeName The store name.
     * @param filePath The file path.
     */
    private suspend fun uploadFileToStore(
        fileSearch: FileSearch,
        storeName: String,
        filePath: String,
    ) {
        var operation =
            fileSearch.uploadToFileSearchStore(
                fileSearchStoreName = storeName,
                file = Path(filePath),
                mimeType = "text/plain",
                uploadRequest =
                    UploadFileSearchStoreRequest(
                        displayName = "file-name",
                        chunkingConfig =
                            ChunkingConfig(
                                whiteSpaceConfig =
                                    WhiteSpaceConfig(
                                        maxTokensPerChunk = 200,
                                        maxOverlapTokens = 20,
                                    ),
                            ),
                    ),
            )

        // 3. Poll operation
        while (operation.done != true) {
            println("Waiting for operation to complete...")
            val delayTime = 5000L
            delay(delayTime)
            operation = fileSearch.getFileSearchStoreOperation(operation.name!!)
        }
        println("Upload complete.")
    }
}
