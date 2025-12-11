package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a collection of models, encapsulating them in a list structure for
 * easy management and access.
 *
 * @property models A list of [Model] objects, each representing a distinct model
 * within the collection. This allows for grouping multiple models together for
 * operations such as batch processing or collective analysis.
 */
@Serializable
data class ModelCollection(
    val models: List<Model>,
)
