package com.avito.android.proguard_guard.configuration.comparator

import proguard.ClassPathEntry

internal object ClassPathEntryComparator : Comparator<ClassPathEntry> {
    private val nullFilter = Comparator.nullsFirst(
        NonNullClassPathEntryComparator()
    )

    override fun compare(left: ClassPathEntry?, right: ClassPathEntry?): Int {
        return nullFilter.compare(left, right)
    }
}

private class NonNullClassPathEntryComparator : Comparator<ClassPathEntry> {
    override fun compare(left: ClassPathEntry, right: ClassPathEntry): Int {
        return left.compare(right)
    }
}

private fun ClassPathEntry.compare(other: ClassPathEntry): Int {
    file.compareWith(other.file).ifDifferent { return it }
    isOutput.compareWith(other.isOutput).ifDifferent { return it }
    name.compareWith(other.name).ifDifferent { return it }
    filter.compareListWith<String>(other.filter).ifDifferent { return it }
    apkFilter.compareListWith<String>(other.apkFilter).ifDifferent { return it }
    jarFilter.compareListWith<String>(other.jarFilter).ifDifferent { return it }
    aarFilter.compareListWith<String>(other.aarFilter).ifDifferent { return it }
    warFilter.compareListWith<String>(other.warFilter).ifDifferent { return it }
    earFilter.compareListWith<String>(other.earFilter).ifDifferent { return it }
    jmodFilter.compareListWith<String>(other.jmodFilter).ifDifferent { return it }
    zipFilter.compareListWith<String>(other.zipFilter).ifDifferent { return it }
    return 0
}
