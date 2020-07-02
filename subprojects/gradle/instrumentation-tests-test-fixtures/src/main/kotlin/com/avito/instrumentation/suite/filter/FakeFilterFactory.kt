package com.avito.instrumentation.suite.filter

val includeAll = object : TestsFilter {
    override val name: String = "FakeIncludeAll"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return TestsFilter.Result.Included
    }
}

fun excludedFilter(reason: TestsFilter.Result.Excluded): TestsFilter {
    return object : TestsFilter {
        override val name: String = "FakeExclude"

        override fun filter(test: TestsFilter.Test): TestsFilter.Result {
            return reason
        }
    }
}

class FakeFilterFactory(
    private val filter: TestsFilter = includeAll
) : FilterFactory {

    override fun createFilter(): TestsFilter {
        return filter
    }
}
