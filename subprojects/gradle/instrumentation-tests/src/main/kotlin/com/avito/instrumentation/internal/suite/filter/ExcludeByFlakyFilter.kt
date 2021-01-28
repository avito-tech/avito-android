package com.avito.instrumentation.internal.suite.filter

import com.avito.report.model.Flakiness

// it makes [name] instance field. It needs for gson
internal class ExcludeByFlakyFilter : TestsFilter {

    override val name = "ExcludeByFlakyFilter"

    override fun filter(test: TestsFilter.Test): TestsFilter.Result {
        return if (test.flakiness is Flakiness.Flaky) {
            TestsFilter.Result.Excluded.HasFlakyAnnotation(
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
