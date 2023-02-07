package com.avito.android.proguard_guard.configuration

import com.avito.android.proguard_guard.configuration.comparator.ClassPathEntryComparator
import com.avito.android.proguard_guard.configuration.comparator.ClassSpecificationComparator
import com.avito.android.proguard_guard.configuration.comparator.KeepClassSpecificationComparator
import com.avito.android.proguard_guard.configuration.comparator.MemberSpecificationComparator
import com.avito.android.proguard_guard.configuration.comparator.typedListOf
import proguard.ClassPath
import proguard.ClassPathEntry
import proguard.ClassSpecification
import proguard.Configuration
import proguard.KeepClassSpecification
import proguard.MemberSpecification
import java.io.File

internal fun parseConfigurationSorted(configurationFile: File): Configuration {
    val configuration = parseConfiguration(configurationFile)
    configuration.sortConfigurationInPlace()
    return configuration
}

internal fun Configuration.sortConfigurationInPlace() {
    programJars?.sortClassPathInPlace()
    libraryJars?.sortClassPathInPlace()
    keepDirectories?.sortStringListInPlace()
    keep?.sortKeepClassSpecificationListInPlace()
    whyAreYouKeeping?.sortClassSpecificationListInPlace()
    optimizations?.sortStringListInPlace()
    assumeNoSideEffects?.sortClassSpecificationListInPlace()
    assumeNoExternalSideEffects?.sortClassSpecificationListInPlace()
    assumeNoEscapingParameters?.sortClassSpecificationListInPlace()
    assumeNoExternalReturnValues?.sortClassSpecificationListInPlace()
    assumeValues?.sortClassSpecificationListInPlace()
    keepPackageNames?.sortStringListInPlace()
    keepAttributes?.sortStringListInPlace()
    adaptClassStrings?.sortStringListInPlace()
    adaptResourceFileNames?.sortStringListInPlace()
    adaptResourceFileContents?.sortStringListInPlace()
    note?.sortStringListInPlace()
    warn?.sortStringListInPlace()
}

private fun MutableList<Any?>.sortStringListInPlace() {
    typedListOf<String>().sort()
}

private fun MutableList<Any?>.sortKeepClassSpecificationListInPlace() {
    typedListOf<KeepClassSpecification>().apply {
        forEach { keepClassSpecification ->
            keepClassSpecification.sortKeepClassSpecificationInPlace()
        }
        sortWith(KeepClassSpecificationComparator)
    }
}

private fun MutableList<Any?>.sortClassSpecificationListInPlace() {
    typedListOf<ClassSpecification>().apply {
        forEach { classSpecification ->
            classSpecification.sortClassSpecificationInPlace()
        }
        sortWith(ClassSpecificationComparator)
    }
}

private fun MutableList<Any?>.sortMemberSpecificationListInPlace() {
    typedListOf<MemberSpecification>().apply {
        forEach { memberSpecification ->
            memberSpecification.sortMemberSpecificationInPlace()
        }
        sortWith(MemberSpecificationComparator)
    }
}

private fun ClassPath.sortClassPathInPlace() {
    val classPath = this
    val classPathEntriesCopy = MutableList<ClassPathEntry>(classPath.size()) { index ->
        classPath.get(index)
    }
    classPathEntriesCopy.forEach {
        it.sortClassPathEntryInPlace()
    }
    classPathEntriesCopy.sortWith(ClassPathEntryComparator)

    classPath.clear()
    classPathEntriesCopy.forEach { entry ->
        classPath.add(entry)
    }
}

private fun ClassPathEntry.sortClassPathEntryInPlace() {
    filter?.sortStringListInPlace()
    apkFilter?.sortStringListInPlace()
    jarFilter?.sortStringListInPlace()
    aarFilter?.sortStringListInPlace()
    warFilter?.sortStringListInPlace()
    earFilter?.sortStringListInPlace()
    jmodFilter?.sortStringListInPlace()
    zipFilter?.sortStringListInPlace()
}

private fun KeepClassSpecification.sortKeepClassSpecificationInPlace() {
    condition?.sortClassSpecificationInPlace()
    (this as ClassSpecification).sortClassSpecificationInPlace()
}

private fun ClassSpecification.sortClassSpecificationInPlace() {
    attributeNames?.sortStringListInPlace()
    fieldSpecifications?.sortMemberSpecificationListInPlace()
    methodSpecifications?.sortMemberSpecificationListInPlace()
}

private fun MemberSpecification.sortMemberSpecificationInPlace() {
    attributeNames?.sortStringListInPlace()
}
