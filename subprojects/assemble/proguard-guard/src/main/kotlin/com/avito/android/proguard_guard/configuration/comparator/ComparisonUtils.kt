@file:Suppress("MatchingDeclarationName")

package com.avito.android.proguard_guard.configuration.comparator

internal enum class ComparisonResult {
    LEFT_GREATER,
    RIGHT_GREATER,
    EQUALS
}

internal inline fun ComparisonResult.ifDifferent(block: (Int) -> Unit) {
    when (this) {
        ComparisonResult.LEFT_GREATER -> block(1)
        ComparisonResult.RIGHT_GREATER -> block(-1)
        else -> {}
    }
}

internal fun Int.toComparisonResult(): ComparisonResult = when {
    this > 0 -> ComparisonResult.LEFT_GREATER
    this < 0 -> ComparisonResult.RIGHT_GREATER
    else -> ComparisonResult.EQUALS
}

internal fun <T> T?.compareWith(
    other: T?,
    comparator: Comparator<T>
): ComparisonResult {
    return when {
        this == null && other != null -> ComparisonResult.RIGHT_GREATER
        this != null && other == null -> ComparisonResult.LEFT_GREATER
        this != null && other != null ->
            comparator.compare(this, other).toComparisonResult()

        else -> ComparisonResult.EQUALS
    }
}

internal fun <T : Comparable<T>> T?.compareWith(other: T?): ComparisonResult {
    return compareWith(other, Comparator.naturalOrder())
}

internal fun <T> MutableList<Any?>?.compareListWith(
    other: MutableList<Any?>?,
    comparator: Comparator<T>
): ComparisonResult {
    return when {
        this.isNullOrEmpty() && !other.isNullOrEmpty() -> ComparisonResult.RIGHT_GREATER
        !this.isNullOrEmpty() && other.isNullOrEmpty() -> ComparisonResult.LEFT_GREATER
        !this.isNullOrEmpty() && !other.isNullOrEmpty() -> {
            val typedLeft = typedListOf<T>()
            val typedRight = other.typedListOf<T>()
            val minLength = minOf(size, other.size)
            (0 until minLength).forEach { index ->
                val comparisonResult = comparator.compare(typedLeft[index], typedRight[index])
                if (comparisonResult != 0) {
                    return comparisonResult.toComparisonResult()
                }
            }
            return (size - other.size).toComparisonResult()
        }

        else -> ComparisonResult.EQUALS
    }
}

internal fun <T : Comparable<T>> MutableList<Any?>?.compareListWith(
    other: MutableList<Any?>?
): ComparisonResult {
    return compareListWith(other, Comparator.naturalOrder<T>())
}

@Suppress("UNCHECKED_CAST")
internal fun <T> MutableList<Any?>.typedListOf(): MutableList<T> {
    return this as MutableList<T>
}
