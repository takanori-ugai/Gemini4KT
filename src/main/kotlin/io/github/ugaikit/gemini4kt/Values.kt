package io.github.ugaikit.gemini4kt

import kotlinx.serialization.Serializable

/**
 * Represents a container for a list of numeric values.
 *
 * This data class is designed to encapsulate a list of [Double] values, providing a structured way
 * to pass around collections of numeric data. It can be used in various contexts where a simple,
 * serializable list of numbers is required.
 *
 * @property values A list of [Double] representing the numeric values contained within this instance.
 */
@Serializable
data class Values(val values: List<Double>)

class ValuesBuilder {
    private val values: MutableList<Double> = mutableListOf()

    fun value(value: Double) {
        values.add(value)
    }

    fun build() = Values(values)
}

fun values(init: ValuesBuilder.() -> Unit): Values = ValuesBuilder().apply(init).build()
