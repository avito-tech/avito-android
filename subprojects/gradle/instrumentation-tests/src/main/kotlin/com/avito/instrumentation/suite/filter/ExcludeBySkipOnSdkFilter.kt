package com.avito.instrumentation.suite.filter

// it makes [name] instance field. It needs for gson
internal class ExcludeBySkipOnSdkFilter : TestsFilter {

    override val name = "ExcludeBySdkFilter"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        val testAnnotations = test.annotations
            .find {
                it.name == "com.avito.android.test.annotations.SkipOnSdk"
            }

        @Suppress("UNCHECKED_CAST")
        val skippedSdks = testAnnotations?.values?.get("sdk") as? Collection<Int>

        return if (skippedSdks?.contains(test.api) == true) {
            TestsFilter.Result.Excluded.HaveSkipSdkAnnotation(
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