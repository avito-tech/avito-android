package com.avito.android.proguard_guard.configuration.comparator

import proguard.ClassSpecification

internal object ClassSpecificationComparator : Comparator<ClassSpecification> {
    private val nullFilter = Comparator.nullsFirst(
        NonNullClassSpecificationComparator()
    )

    override fun compare(left: ClassSpecification?, right: ClassSpecification?): Int {
        return nullFilter.compare(left, right)
    }
}

private class NonNullClassSpecificationComparator : Comparator<ClassSpecification> {
    override fun compare(left: ClassSpecification, right: ClassSpecification): Int {
        return left.compare(right)
    }
}

private fun ClassSpecification.compare(other: ClassSpecification): Int {
    requiredSetAccessFlags.compareWith(other.requiredSetAccessFlags).ifDifferent { return it }
    requiredUnsetAccessFlags.compareWith(other.requiredUnsetAccessFlags).ifDifferent { return it }
    annotationType.compareWith(other.annotationType).ifDifferent { return it }
    className.compareWith(other.className).ifDifferent { return it }
    extendsAnnotationType.compareWith(other.extendsAnnotationType).ifDifferent { return it }
    extendsClassName.compareWith(other.extendsClassName).ifDifferent { return it }
    attributeNames.compareListWith<String>(other.attributeNames).ifDifferent { return it }

    fieldSpecifications.compareListWith(
        other.fieldSpecifications,
        comparator = MemberSpecificationComparator
    ).ifDifferent { return it }
    methodSpecifications.compareListWith(
        other.methodSpecifications,
        comparator = MemberSpecificationComparator
    ).ifDifferent { return it }
    return 0
}
