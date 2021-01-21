package com.avito.math

fun <N : Number> Iterable<N>.median() = asSequence().median()

fun <N : Number> Array<out N>.median() = asSequence().median()

/**
 * The algorithm is quite ineffective
 * see https://stackoverflow.com/a/28822243/981330
 */
fun <N : Number> Sequence<N>.median(): Double {

    val sorted = map { it.toDouble() }.sorted().toList()

    require(sorted.isNotEmpty()) { "Cannot calculate median of no elements" }

    return if (sorted.size % 2 == 0) {
        (sorted[sorted.size / 2] + sorted[sorted.size / 2 - 1]) / 2
    } else {
        sorted[sorted.size / 2]
    }
}
