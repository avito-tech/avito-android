package com.avito.instrumentation.suite.filter

internal class CompositionFilter(
    private val filters: List<TestsFilter>
) : TestsFilter {

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return filters
            .map { it.filter(test) }
            .firstOrNull { it is TestsFilter.Result.Excluded } ?: TestsFilter.Result.Included
    }
}