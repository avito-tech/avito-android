package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName

class PackagePrefixFilter(private val prefixFilter: String) : TestRunFilter {

    override fun runNeeded(test: TestInApk, deviceName: DeviceName, api: Int): TestRunFilter.Verdict {
        return if (test.testName.name.startsWith(prefixFilter)) {
            TestRunFilter.Verdict.Run
        } else {
            TestRunFilter.Verdict.Skip.NotHasPrefix(prefix = prefixFilter)
        }
    }
}
