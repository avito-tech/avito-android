package com.avito.runner.scheduler

import com.avito.runner.exit.Exit
import com.avito.runner.exit.ExitManager
import com.avito.runner.logging.StdOutLogger
import com.avito.runner.scheduler.report.Reporter
import com.avito.runner.scheduler.report.SummaryReportMaker
import com.avito.runner.scheduler.report.model.SummaryReport
import com.avito.runner.scheduler.report.model.TestCaseRequestMatchingReport
import com.avito.runner.scheduler.runner.TestRunner
import com.avito.runner.scheduler.runner.TestRunnerResult
import com.avito.runner.scheduler.util.generateTestRunRequest
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.test.generateDeviceData
import com.avito.runner.test.generateDeviceTestCaseRun
import com.avito.runner.test.generateTestCase
import com.avito.runner.test.generateTestCaseRun
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class EntrypointTest {

    @Mock
    private lateinit var testRunner: TestRunner

    @Mock
    private lateinit var summaryMaker: SummaryReportMaker

    @Mock
    private lateinit var reporter: Reporter

    @Mock
    private lateinit var exitManager: ExitManager

    private lateinit var entrypoint: Entrypoint

    @BeforeEach
    fun setUp() {
        entrypoint = Entrypoint(
            testRunner = testRunner,
            summaryReportMaker = summaryMaker,
            reporter = reporter,
            exitManager = exitManager,
            logger = StdOutLogger(),
            failOnFailedTests = true
        )
    }

    @Test
    fun `run - creates report based on test runner runs`() {
        val runs = createTestRunnerResult()
        givenTestRuns(
            runs = runs
        )
        givenReport(
            forResult = runs,
            tests = listOf()
        )

        run()

        verify(summaryMaker).make(eq(runs), any())
    }

    @Test
    fun `run - execute report`() {
        val runs = createTestRunnerResult()
        givenTestRuns(
            runs = runs
        )
        givenReport(
            forResult = runs,
            tests = listOf()
        )

        run()

        verify(reporter).report(any())
    }

    @Test
    fun `run - exits with there were failed tests status - at least one failed test`() {
        val runs = createTestRunnerResult()
        givenTestRuns(
            runs = runs
        )
        givenReport(
            forResult = runs,
            tests = listOf(createFailedTestReport())
        )

        run()

        verify(exitManager).exit(Exit.ThereWereFailedTests)
    }

    private fun createFailedTestReport(): TestCaseRequestMatchingReport =
        TestCaseRequestMatchingReport(
            request = generateTestRunRequest(),
            runs = listOf(
                DeviceTestCaseRun(
                    testCaseRun = generateTestCaseRun(
                        result = TestCaseRun.Result.Failed("failed")
                    ),
                    device = generateDeviceData()
                )
            ),
            result = TestCaseRequestMatchingReport.Result.Mismatched
        )

    private fun createTestRunnerResult(): TestRunnerResult {
        val testCase = generateTestCase()
        val testRunRequest = generateTestRunRequest(
            testCase = testCase
        )
        val runs = listOf(
            testRunRequest to listOf(
                generateDeviceTestCaseRun(
                    testCaseRun = generateTestCaseRun(
                        testCase = testCase,
                        result = TestCaseRun.Result.Passed
                    )
                )
            )
        )

        return TestRunnerResult(
            runs = runs.toMap()
        )
    }

    private fun givenReport(
        forResult: TestRunnerResult,
        tests: List<TestCaseRequestMatchingReport> = emptyList(),
        durationMilliseconds: Long = 0
    ) {
        whenever(summaryMaker.make(eq(forResult), any())).thenReturn(
            SummaryReport(
                reports = tests,
                durationMilliseconds = durationMilliseconds
            )
        )
    }

    private fun givenTestRuns(runs: TestRunnerResult) =
        runBlocking {
            whenever(testRunner.runTests(any())).thenReturn(runs)
        }

    private fun run() {
        entrypoint.run(
            requests = emptyList()
        )
    }
}
