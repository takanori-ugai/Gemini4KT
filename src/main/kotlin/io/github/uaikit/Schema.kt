package io.github.uaikit

import kotlinx.serialization.Serializable

@Serializable
data class Schema(
    val type: String,
    val format: String,
    val description: String,
    val nullable: Boolean,
    val enum: List<String>,
    val properties: Map<String, Schema>,
    val required: List<String>,
    val items: Schema,
)
