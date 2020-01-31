package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName

class CompositeTestRunFilter(private val filters: List<TestRunFilter>) : TestRunFilter {

    override fun runNeeded(test: TestInApk, deviceName: DeviceName, api: Int): TestRunFilter.Verdict =
        filters.asSequence()
            .map { filter -> filter.runNeeded(test, deviceName, api) }
            .firstOrNull { verdict -> verdict is TestRunFilter.Verdict.Skip }
            ?: TestRunFilter.Verdict.Run
}
