package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.Serializable

/**
 * Request body for uploading to a FileSearchStore.
 *
 * @property displayName Optional. Display name of the created document.
 * @property customMetadata Custom metadata to be associated with the data.
 * @property chunkingConfig Config for telling the service how to chunk the data.
 * @property mimeType Optional. MIME type of the data.
 */
@Serializable
data class UploadFileSearchStoreRequest(
    val displayName: String? = null,
    val customMetadata: List<CustomMetadata>? = null,
    val chunkingConfig: ChunkingConfig? = null,
    val mimeType: String? = null
)
