package com.avito.report.inmemory

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.Report
import com.avito.report.ReportLinksGenerator
import com.avito.report.TestSuiteNameProvider
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestAttempt
import com.avito.report.model.TestStaticData
import com.avito.report.model.TestStatus
import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.test.model.TestName
import com.avito.time.TimeProvider

internal class InMemoryReport(
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory,
    private val testAttemptsAggregateStrategy: TestAttemptsAggregateStrategy,
    override val reportLinksGenerator: ReportLinksGenerator,
    override val testSuiteNameProvider: TestSuiteNameProvider,
) : Report {

    private val logger = loggerFactory.create<InMemoryReport>()

    private val testAttempts: MutableList<TestAttempt> = mutableListOf()

    @Synchronized
    override fun addTest(testAttempt: TestAttempt) {
        logger.debug("addTest $testAttempt")
        this.testAttempts.add(testAttempt)
    }

    @Synchronized
    override fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        logger.debug("addSkippedTests $skippedTests")

        this.testAttempts.addAll(
            skippedTests.map { (test, reason) ->
                TestAttempt.createWithoutExecution(
                    AndroidTest.Skipped.fromTestMetadata(
                        testStaticData = test,
                        skipReason = reason,
                        reportTime = timeProvider.nowInSeconds()
                    )
                )
            }
        )
    }

    /**
     * lost tests determined via [getTestResults] and can be found in [com.avito.runner.finalizer.verdict.Verdict]
     */
    override fun reportLostTests(notReportedTests: Collection<AndroidTest.Lost>) {
        // no action needed for inMemory report here
    }

    @Synchronized
    override fun getTestResults(): Collection<AndroidTest> {
        val grouped: Map<TestKey, List<TestAttempt>> =
            testAttempts.groupBy(
                keySelector = {
                    TestKey(
                        testName = it.testResult.name,
                        deviceName = it.testResult.device
                    )
                }
            )

        return grouped.mapValues { (_, executions) ->
            testAttemptsAggregateStrategy.getTestResult(executions)
        }.values
    }

    /**
     * not available for InMemoryReport
     */
    override fun getPreviousRunsResults(): Result<Map<TestCase, TestStatus>> {
        return Result.Success(emptyMap())
    }

    private data class TestKey(val testName: TestName, val deviceName: DeviceName)
}
