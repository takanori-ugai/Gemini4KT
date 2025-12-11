package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.Serializable

/**
 * Request body for importing a File from File Service to a FileSearchStore.
 *
 * @property fileName Required. The name of the File to import.
 * @property customMetadata Custom metadata to be associated with the file.
 * @property chunkingConfig Config for telling the service how to chunk the file.
 */
@Serializable
data class ImportFileRequest(
    val fileName: String,
    val customMetadata: List<CustomMetadata>? = null,
    val chunkingConfig: ChunkingConfig? = null,
)
