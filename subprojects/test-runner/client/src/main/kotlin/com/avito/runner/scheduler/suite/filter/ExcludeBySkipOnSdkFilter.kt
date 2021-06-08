package com.avito.runner.scheduler.suite.filter

import com.avito.android.test.annotations.SkipOnSdk

// it makes [name] instance field. It needs for gson
internal class ExcludeBySkipOnSdkFilter : TestsFilter {

    override val name = "ExcludeBySkipOnSdkFilter"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        val testAnnotations = test.annotations
            .find {
                it.name == SkipOnSdk::class.java.name
            }

        @Suppress("UNCHECKED_CAST")
        val skippedSdks = testAnnotations?.values?.get("sdk") as? Collection<Int>

        return if (skippedSdks?.contains(test.api) == true) {
            TestsFilter.Result.Excluded.HasSkipSdkAnnotation(
                name,
                test.api
            )
        } else {
            TestsFilter.Result.Included
        }
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other.hashCode() == hashCode() && other.javaClass.isInstance(this)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
