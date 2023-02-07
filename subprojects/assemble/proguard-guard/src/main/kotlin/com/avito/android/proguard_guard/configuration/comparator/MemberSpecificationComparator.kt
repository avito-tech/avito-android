package com.avito.android.proguard_guard.configuration.comparator

import proguard.MemberSpecification

internal object MemberSpecificationComparator : Comparator<MemberSpecification> {
    private val nullFilter = Comparator.nullsFirst(
        NonNullMemberSpecificationComparator()
    )

    override fun compare(left: MemberSpecification?, right: MemberSpecification?): Int {
        return nullFilter.compare(left, right)
    }
}

private class NonNullMemberSpecificationComparator : Comparator<MemberSpecification> {
    override fun compare(left: MemberSpecification, right: MemberSpecification): Int {
        return left.compare(right)
    }
}

private fun MemberSpecification.compare(other: MemberSpecification): Int {
    requiredSetAccessFlags.compareWith(other.requiredSetAccessFlags).ifDifferent { return it }
    requiredUnsetAccessFlags.compareWith(other.requiredUnsetAccessFlags).ifDifferent { return it }
    annotationType.compareWith(other.annotationType).ifDifferent { return it }
    name.compareWith(other.name).ifDifferent { return it }
    descriptor.compareWith(other.descriptor).ifDifferent { return it }
    attributeNames.compareListWith<String>(other.attributeNames).ifDifferent { return it }
    return 0
}
