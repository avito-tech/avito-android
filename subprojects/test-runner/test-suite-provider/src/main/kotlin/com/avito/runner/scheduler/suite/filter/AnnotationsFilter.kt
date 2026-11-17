package com.avito.runner.scheduler.suite.filter

import com.avito.runner.scheduler.suite.filter.TestsFilter.Verdict.Skipped

internal fun TestsFilter.Test.matched(annotations: Set<String>): Boolean {
    return this.annotations.any { testAnnotation -> annotations.contains(testAnnotation.name) }
}

internal data class IncludeAnnotationsFilter(
    private val annotations: Set<String>
) : TestsFilter {

    override val name = "IncludeAnnotations"

    override fun filter(test: TestsFilter.Test): TestsFilter.Verdict {
        return when {
            test.matched(annotations) -> TestsFilter.Verdict.Included
            else -> Skipped.DoesNotHaveIncludeAnnotations(name, annotations)
        }
    }
}

internal data class ExcludeAnnotationsFilter(
    private val annotations: Set<String>
) : TestsFilter {

    override val name = "ExcludeAnnotations"

    override fun filter(test: TestsFilter.Test): TestsFilter.Verdict {
        return when {
            test.matched(annotations) -> Skipped.HasExcludeAnnotations(name, annotations)
            else -> TestsFilter.Verdict.Included
        }
    }
}
