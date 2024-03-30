package io.github.ugaikit

/**
 * Enumerates the types of tasks that can be performed.
 *
 * @property TASK_TYPE_UNSPECIFIED Represents an unspecified task type. Used as a default value.
 * @property RETRIEVAL_QUERY Represents a task type for retrieval queries.
 * @property RETRIEVAL_DOCUMENT Represents a task type for document retrieval.
 * @property SEMANTIC_SIMILARITY Represents a task type for measuring semantic similarity.
 * @property CLASSIFICATION Represents a task type for classification tasks.
 * @property CLUSTERING Represents a task type for clustering tasks.
 */
enum class TaskType {
    TASK_TYPE_UNSPECIFIED,
    RETRIEVAL_QUERY,
    RETRIEVAL_DOCUMENT,
    SEMANTIC_SIMILARITY,
    CLASSIFICATION,
    CLUSTERING,
}
