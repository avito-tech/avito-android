package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.internal.suite.filter.FilterFactory
import com.avito.instrumentation.internal.suite.filter.TestsFilter

internal val includeAll = object : TestsFilter {
    override val name: String = "StubIncludeAll"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return TestsFilter.Result.Included
    }
}

internal fun excludedFilter(reason: TestsFilter.Result.Excluded): TestsFilter {
    return object : TestsFilter {
        override val name: String = "StubExclude"

        override fun filter(test: TestsFilter.Test): TestsFilter.Result {
            return reason
        }
    }
}

internal class StubFilterFactory(
    private val filter: TestsFilter = includeAll
) : FilterFactory {

    override fun createFilter(): TestsFilter {
        return filter
    }
}
