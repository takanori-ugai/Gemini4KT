package io.github.ugaikit.gemini4kt

/**
 * Compares nullable arrays by contents while treating nulls as equal.
 */
internal fun <T> Array<T>?.contentEqualsNullable(other: Array<T>?): Boolean =
    when {
        this == null && other == null -> true
        this == null || other == null -> false
        else -> this.contentEquals(other)
    }
