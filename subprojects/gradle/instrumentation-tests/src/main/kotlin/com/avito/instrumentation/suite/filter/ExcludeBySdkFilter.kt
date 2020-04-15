package com.avito.instrumentation.suite.filter

internal object ExcludeBySdkFilter : TestsFilter {

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
}