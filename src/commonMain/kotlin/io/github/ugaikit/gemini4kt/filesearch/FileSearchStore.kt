package io.github.ugaikit.gemini4kt.filesearch

import kotlinx.serialization.Serializable

/**
 * A FileSearchStore is a collection of Documents.
 *
 * @property name Output only. Immutable. Identifier. The FileSearchStore resource name.
 * @property displayName Optional. The human-readable display name for the FileSearchStore.
 * @property createTime Output only. The Timestamp of when the FileSearchStore was created.
 * @property updateTime Output only. The Timestamp of when the FileSearchStore was last updated.
 * @property activeDocumentsCount Output only. The number of documents in the FileSearchStore that are active and ready for retrieval.
 * @property pendingDocumentsCount Output only. The number of documents in the FileSearchStore that are being processed.
 * @property failedDocumentsCount Output only. The number of documents in the FileSearchStore that have failed processing.
 * @property sizeBytes Output only. The size of raw bytes ingested into the FileSearchStore.
 */
@Serializable
data class FileSearchStore(
    val name: String? = null,
    val displayName: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val activeDocumentsCount: String? = null,
    val pendingDocumentsCount: String? = null,
    val failedDocumentsCount: String? = null,
    val sizeBytes: String? = null,
)
