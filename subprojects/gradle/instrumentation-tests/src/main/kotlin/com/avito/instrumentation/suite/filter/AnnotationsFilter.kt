package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.filter.TestsFilter.Result.Excluded

internal fun TestsFilter.Test.matched(annotations: Set<String>): Boolean {
    return this.annotations.any { testAnnotation -> annotations.contains(testAnnotation.name) }
}

internal data class IncludeAnnotationsFilter(
    private val annotations: Set<String>
) : TestsFilter {

    override val name = "IncludeAnnotations"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return when {
            test.matched(annotations) -> TestsFilter.Result.Included
            else -> Excluded.DoNotHaveIncludeAnnotations(name, annotations)
        }
    }
}

internal data class ExcludeAnnotationsFilter(
    private val annotations: Set<String>
) : TestsFilter {

    override val name = "ExcludeAnnotations"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return when {
            test.matched(annotations) -> Excluded.HaveExcludeAnnotations(name, annotations)
            else -> TestsFilter.Result.Included
        }
    }
}