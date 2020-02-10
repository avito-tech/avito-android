package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName
import com.avito.report.model.SimpleRunTest

class OnlyFailedTestsFilter(private val testRunResults: List<SimpleRunTest>) : TestRunFilter {

    override fun runNeeded(test: TestInApk, deviceName: DeviceName, api: Int): TestRunFilter.Verdict {
        val testResultInPreviousReport = testRunResults.singleOrNull { isSameTest(test, it, deviceName) }

        return if (testResultInPreviousReport == null) {
            TestRunFilter.Verdict.Run
        } else {
            // conclusion (пометка о том что упавший тест на самом деле ок) учтен в status
            if (testResultInPreviousReport.status.isSuccessful) {
                TestRunFilter.Verdict.Skip.OnlyFailed(status = testResultInPreviousReport.status)
            } else {
                TestRunFilter.Verdict.Run
            }
        }
    }

    private fun isSameTest(test: TestInApk, testResult: SimpleRunTest, deviceName: DeviceName): Boolean {
        return test.testName.name == testResult.name && testResult.deviceName == deviceName.name
    }
}
