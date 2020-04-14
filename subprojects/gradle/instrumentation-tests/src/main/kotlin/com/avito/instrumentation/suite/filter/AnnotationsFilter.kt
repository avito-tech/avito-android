package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.filter.TestsFilter.Result.Excluded

internal fun TestsFilter.Test.matched(annotations: Set<String>): Boolean {
    return this.annotations.any { testAnnotation -> annotations.contains(testAnnotation.name) }
}

internal class IncludeAnnotationsFilter(
    private val annotations: Set<String>
) : TestsFilter {

    override val name = "IncludeAnnotations"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return when {
            test.matched(annotations) -> TestsFilter.Result.Included
            else -> Excluded.DoNotHaveIncludeAnnotations(annotations)
        }
    }
}

internal class ExcludeAnnotationsFilter(
    private val annotations: Set<String>
) : TestsFilter {

    override val name = "ExcludeAnnotations"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return when {
            test.matched(annotations) -> Excluded.HaveExcludeAnnotations(annotations)
            else -> TestsFilter.Result.Included
        }
    }
}