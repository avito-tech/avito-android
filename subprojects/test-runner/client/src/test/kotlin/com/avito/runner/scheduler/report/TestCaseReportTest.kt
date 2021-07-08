package com.avito.runner.scheduler.report

import com.avito.runner.model.TestCaseRun
import com.avito.runner.scheduler.report.model.TestCaseRequestMatchingReport
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.createStubInstance
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestCaseReportTest {

    @Test
    fun `success runs - returns number of passed test runs`() {
        val report = createTestCaseReport(
            runResults = listOf(
                TestCaseRun.Result.Passed,
                TestCaseRun.Result.Passed,
                TestCaseRun.Result.Failed.InRun("")
            )
        )

        assertThat(report.successRuns).isEqualTo(2)
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

        assertThat(report.failedRuns).isEqualTo(2)
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

        assertThat(report.totalRuns).isEqualTo(3)
    }

    @Test
    fun `duration - returns sum of run durations`() {
        val durations = listOf(100L, 200L, 300L)
        val report = createTestCaseReportWithDurations(durations)

        assertThat(report.durationMilliseconds).isEqualTo(600L)
    }

    private fun createTestCaseReport(runResults: List<TestCaseRun.Result>): TestCaseRequestMatchingReport =
        TestCaseRequestMatchingReport(
            request = TestRunRequest.Companion.createStubInstance(),
            runs = runResults.map {
                DeviceTestCaseRun.createStubInstance(
                    testCaseRun = TestCaseRun.createStubInstance(
                        result = it
                    )
                )
            },
            result = TestCaseRequestMatchingReport.Result.Matched
        )

    private fun createTestCaseReportWithDurations(durations: List<Long>): TestCaseRequestMatchingReport =
        TestCaseRequestMatchingReport(
            request = TestRunRequest.Companion.createStubInstance(),
            runs = durations.map {
                DeviceTestCaseRun.createStubInstance(
                    testCaseRun = TestCaseRun.createStubInstance(
                        timestampStartedMilliseconds = 0,
                        timestampCompletedMilliseconds = it
                    )
                )
            },
            result = TestCaseRequestMatchingReport.Result.Matched
        )
}
