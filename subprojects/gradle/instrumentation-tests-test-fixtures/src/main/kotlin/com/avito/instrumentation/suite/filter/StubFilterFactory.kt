package com.avito.instrumentation.suite.filter

val includeAll = object : TestsFilter {
    override val name: String = "StubIncludeAll"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return TestsFilter.Result.Included
    }
}

fun excludedFilter(reason: TestsFilter.Result.Excluded): TestsFilter {
    return object : TestsFilter {
        override val name: String = "StubExclude"

        override fun filter(test: TestsFilter.Test): TestsFilter.Result {
            return reason
        }
    }
}

class StubFilterFactory(
    private val filter: TestsFilter = includeAll
) : FilterFactory {

    override fun createFilter(): TestsFilter {
        return filter
    }
}
