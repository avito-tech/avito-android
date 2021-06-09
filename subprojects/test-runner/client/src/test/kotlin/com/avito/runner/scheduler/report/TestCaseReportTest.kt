package com.avito.runner.scheduler.report

import com.avito.runner.scheduler.report.model.TestCaseRequestMatchingReport
import com.avito.runner.scheduler.runner.model.generateTestRunRequest
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.test.Is
import com.avito.runner.test.generateDeviceTestCaseRun
import com.avito.runner.test.generateTestCaseRun
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class TestCaseReportTest {

    @Test
    fun `success runs - returns number of passed test runs`() {
        val report = createTestCaseReport(
            runResults = listOf(
                TestCaseRun.Result.Passed,
                TestCaseRun.Result.Passed,
                TestCaseRun.Result.Failed.InRun("")
            )
        )

        assertThat(report.successRuns, Is(2))
    }

    @Test
    fun `failed runs - returns number of failed test runs`() {
        val report = createTestCaseReport(
            runResults = listOf(
                TestCaseRun.Result.Passed,
                TestCaseRun.Result.Failed.InRun(""),
                TestCaseRun.Result.Failed.InRun("")
            )
        )

        assertThat(report.failedRuns, Is(2))
    }

    @Test
    fun `total runs - returns number of test runs`() {
        val report = createTestCaseReport(
            runResults = listOf(
                TestCaseRun.Result.Passed,
                TestCaseRun.Result.Failed.InRun(""),
                TestCaseRun.Result.Failed.InRun("")
            )
        )

        assertThat(report.totalRuns, Is(3))
    }

    @Test
    fun `duration - returns sum of run durations`() {
        val durations = listOf(100L, 200L, 300L)
        val report = createTestCaseReportWithDurations(durations)

        assertThat(report.durationMilliseconds, Is(600L))
    }

    private fun createTestCaseReport(runResults: List<TestCaseRun.Result>): TestCaseRequestMatchingReport =
        TestCaseRequestMatchingReport(
            request = generateTestRunRequest(),
            runs = runResults.map {
                generateDeviceTestCaseRun(
                    testCaseRun = generateTestCaseRun(
                        result = it
                    )
                )
            },
            result = TestCaseRequestMatchingReport.Result.Matched
        )

    private fun createTestCaseReportWithDurations(durations: List<Long>): TestCaseRequestMatchingReport =
        TestCaseRequestMatchingReport(
            request = generateTestRunRequest(),
            runs = durations.map {
                generateDeviceTestCaseRun(
                    testCaseRun = generateTestCaseRun(
                        timestampStartedMilliseconds = 0,
                        timestampCompletedMilliseconds = it
                    )
                )
            },
            result = TestCaseRequestMatchingReport.Result.Matched
        )
}
