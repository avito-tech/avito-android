package com.avito.runner.scheduler.report

import com.avito.runner.scheduler.report.model.SummaryReport
import com.avito.runner.scheduler.report.model.TestCaseRequestMatchingReport
import com.avito.runner.scheduler.runner.TestRunnerResult
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.util.generateTestRunRequest
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.test.generateDeviceTestCaseRun
import com.avito.runner.test.generateTestCaseRun
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ReportCreatorTest {

    @Test
    fun `create summary - creates test report for each test case`() {
        val summary = createSummary(
            runs = listOf(
                generateTestRunRequest() to createTestSuiteRuns(),
                generateTestRunRequest() to createTestSuiteRuns(),
                generateTestRunRequest() to createTestSuiteRuns()
            )
        )

        assertThat(summary.reports).hasSize(3)
    }

    @Test
    fun `runs count - is empty`() {
        val summary = createSummary(
            runs = listOf(
                generateTestRunRequest() to createTestSuiteRuns(),
                generateTestRunRequest() to createTestSuiteRuns(),
                generateTestRunRequest() to createTestSuiteRuns()
            )
        )

        assertThat(summary.successRunsCount).isEqualTo(0)
        assertThat(summary.failedRunsCount).isEqualTo(0)
        assertThat(summary.ignoredRunsCount).isEqualTo(0)
    }

    @Test
    fun `create summary - marks test as matched - all tests passed`() {
        val summary = createSummary(
            runs = listOf(
                generateTestRunRequest() to createTestSuiteRuns(
                    TestCaseRun.Result.Passed,
                    TestCaseRun.Result.Passed,
                    TestCaseRun.Result.Passed
                )
            )
        )

        assertThat(
            summary.reports.first().result
        ).isInstanceOf(TestCaseRequestMatchingReport.Result.Matched::class.java)
    }

    @Test
    fun `create summary - marks test as mismatched - all tests failed`() {
        val summary = createSummary(
            runs = listOf(
                generateTestRunRequest() to createTestSuiteRuns(
                    TestCaseRun.Result.Failed(""),
                    TestCaseRun.Result.Failed(""),
                    TestCaseRun.Result.Failed("")
                )
            )
        )

        assertThat(
            summary.reports.first().result
        ).isInstanceOf(TestCaseRequestMatchingReport.Result.Mismatched::class.java)
    }

    @Test
    fun `failed count - is not 0`() {
        val summary = createSummary(
            runs = listOf(
                generateTestRunRequest() to createTestSuiteRuns(
                    TestCaseRun.Result.Failed(""),
                    TestCaseRun.Result.Failed(""),
                    TestCaseRun.Result.Failed("")
                )
            )
        )

        assertThat(summary.successRunsCount).isEqualTo(0)
        assertThat(summary.failedRunsCount).isEqualTo(3)
        assertThat(summary.ignoredRunsCount).isEqualTo(0)
    }

    @Test
    fun `create summary - marks test as mismatched - minimum success test count not reached`() {
        val runs = listOf(
            generateTestRunRequest(
                scheduling = TestRunRequest.Scheduling(
                    retryCount = 2,
                    minimumSuccessCount = 2,
                    minimumFailedCount = 0
                )
            ) to createTestSuiteRuns(
                TestCaseRun.Result.Passed,
                TestCaseRun.Result.Failed(""),
                TestCaseRun.Result.Failed("")
            )
        )

        val summary = createSummary(runs = runs)

        assertThat(
            summary.reports.first().result
        ).isInstanceOf(TestCaseRequestMatchingReport.Result.Mismatched::class.java)
    }

    @Test
    fun `failed and passed count - is not 0`() {
        val runs = listOf(
            generateTestRunRequest(
                scheduling = TestRunRequest.Scheduling(
                    retryCount = 2,
                    minimumSuccessCount = 2,
                    minimumFailedCount = 0
                )
            ) to createTestSuiteRuns(
                TestCaseRun.Result.Passed,
                TestCaseRun.Result.Failed(""),
                TestCaseRun.Result.Failed("")
            )
        )

        val summary = createSummary(runs = runs)

        assertThat(summary.successRunsCount).isEqualTo(1)
        assertThat(summary.failedRunsCount).isEqualTo(2)
        assertThat(summary.ignoredRunsCount).isEqualTo(0)
    }

    @Test
    fun `create summary - marks test as matched - minimum success test count reached`() {
        val runs = listOf(
            generateTestRunRequest(
                scheduling = TestRunRequest.Scheduling(
                    retryCount = 2,
                    minimumSuccessCount = 2,
                    minimumFailedCount = 0
                )
            ) to createTestSuiteRuns(
                TestCaseRun.Result.Passed,
                TestCaseRun.Result.Passed,
                TestCaseRun.Result.Failed("")
            )
        )

        val summary = createSummary(runs = runs)

        assertThat(
            summary.reports.first().result
        ).isInstanceOf(TestCaseRequestMatchingReport.Result.Matched::class.java)
    }

    @Test
    fun `create summary - marks test as ignored - run contains ignored test`() {
        val runs = listOf(
            generateTestRunRequest(
                scheduling = TestRunRequest.Scheduling(
                    retryCount = 2,
                    minimumSuccessCount = 2,
                    minimumFailedCount = 0
                )
            ) to createTestSuiteRuns(
                TestCaseRun.Result.Ignored
            )
        )

        val summary = createSummary(runs = runs)

        assertThat(
            summary.reports.first().result
        ).isInstanceOf(TestCaseRequestMatchingReport.Result.Ignored::class.java)
    }

    private fun createTestSuiteRuns(
        vararg results: TestCaseRun.Result
    ): List<DeviceTestCaseRun> = results.map {
        generateDeviceTestCaseRun(
            testCaseRun = generateTestCaseRun(
                result = it
            )
        )
    }

    private fun createSummary(
        runs: List<Pair<TestRunRequest, List<DeviceTestCaseRun>>> = listOf(),
        startTimeMilliseconds: Long = 0
    ): SummaryReport {
        val reportCreator = SummaryReportMakerImplementation()
        return reportCreator.make(
            runResult = TestRunnerResult(
                runs = runs.toMap()
            ),
            startTimeMilliseconds = startTimeMilliseconds
        )
    }
}
