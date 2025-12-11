package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents the response from an embedding operation, containing the computed
 * embedding values.
 *
 * @property embedding A [Values] object that holds the numerical representation
 * of the embedded data. This could be a vector or set of values resulting from
 * processing the input through an embedding model or algorithm.
 */
@Serializable
data class EmbedResponse(
    val embedding: Values,
)
