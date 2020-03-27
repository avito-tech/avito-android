package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName
import com.avito.report.model.SimpleRunTest

class TestRunsFilter private constructor(
    private val testRuns: List<SimpleRunTest>,
    private val needRunTestWithoutTestRun: Boolean
) : TestRunFilter {

    override fun runNeeded(
        test: TestInApk,
        deviceName: DeviceName,
        api: Int
    ): TestRunFilter.Verdict {
        val testRun = findTestRunBy(test, deviceName)
        return when {
            testRun == null -> {
                if (needRunTestWithoutTestRun) {
                    TestRunFilter.Verdict.Run
                } else {
                    TestRunFilter.Verdict.Skip.ByPreviousTestRun.TestRunIsAbsent
                }
            }
            !testRun.status.isSuccessful -> TestRunFilter.Verdict.Run
            else -> TestRunFilter.Verdict.Skip.ByPreviousTestRun.TestRunIsSucceed(testRun.status)
        }
    }

    private fun findTestRunBy(
        test: TestInApk,
        deviceName: DeviceName
    ): SimpleRunTest? = testRuns.singleOrNull { testRun ->
        test.testName.name == testRun.name && testRun.deviceName == deviceName.name
    }

    companion object {
        fun keepFailedTestRuns(testRuns: List<SimpleRunTest>): TestRunFilter {
            return TestRunsFilter(
                testRuns = testRuns,
                needRunTestWithoutTestRun = false
            )
        }

        fun skipSucceedTestRuns(testRuns: List<SimpleRunTest>): TestRunFilter {
            return TestRunsFilter(
                testRuns = testRuns,
                needRunTestWithoutTestRun = true
            )
        }
    }
}