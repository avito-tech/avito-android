package com.avito.android.proguard_guard.configuration.comparator

import proguard.KeepClassSpecification

internal object KeepClassSpecificationComparator : Comparator<KeepClassSpecification> {
    private val nullFilter = Comparator.nullsFirst(
        NonNullKeepClassSpecificationComparator()
    )

    override fun compare(left: KeepClassSpecification?, right: KeepClassSpecification?): Int {
        return nullFilter.compare(left, right)
    }
}

private class NonNullKeepClassSpecificationComparator : Comparator<KeepClassSpecification> {
    override fun compare(left: KeepClassSpecification, right: KeepClassSpecification): Int {
        return left.compare(right)
    }
}

private fun KeepClassSpecification.compare(other: KeepClassSpecification): Int {
    markClasses.compareWith(other.markClasses).ifDifferent { return it }
    markConditionally.compareWith(other.markConditionally).ifDifferent { return it }
    markDescriptorClasses.compareWith(other.markDescriptorClasses).ifDifferent { return it }
    markCodeAttributes.compareWith(other.markCodeAttributes).ifDifferent { return it }
    allowShrinking.compareWith(other.allowShrinking).ifDifferent { return it }
    allowOptimization.compareWith(other.allowOptimization).ifDifferent { return it }
    allowObfuscation.compareWith(other.allowObfuscation).ifDifferent { return it }

    condition.compareWith(
        other.condition,
        comparator = ClassSpecificationComparator
    ).ifDifferent { return it }
    this.compareWith(
        other,
        comparator = ClassSpecificationComparator
    ).ifDifferent { return it }
    return 0
}
