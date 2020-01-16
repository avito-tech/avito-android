package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName

class NameTestsFilter(private val testsToRun: List<String>?) : TestRunFilter {

    override fun runNeeded(test: TestInApk, deviceName: DeviceName, api: Int): TestRunFilter.Verdict =
        if (testsToRun == null || testsToRun.isEmpty()) {
            TestRunFilter.Verdict.Run
        } else {
            if (testsToRun.contains(test.testName.name)) {
                TestRunFilter.Verdict.Run
            } else {
                TestRunFilter.Verdict.Skip.NotSpecifiedInTestsToRun
            }
        }
}
