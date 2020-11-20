package com.avito.instrumentation.suite.filter

import com.google.common.annotations.VisibleForTesting

internal class CompositionFilter(
    @VisibleForTesting
    internal val filters: List<TestsFilter>
) : TestsFilter {

    override val name = "Composition"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return filters
            .map { it.filter(test) }
            .firstOrNull { it is TestsFilter.Result.Excluded } ?: TestsFilter.Result.Included
    }
}
