package com.avito.android.runner.report.internal

import com.avito.android.runner.report.Report
import com.avito.android.runner.report.TestAttempt
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData
import com.avito.time.TimeProvider

internal class InMemoryReport(
    private val timeProvider: TimeProvider,
    private val testAttemptsAggregateStrategy: TestAttemptsAggregateStrategy
) : Report {

    private val testAttempts: MutableList<TestAttempt> = mutableListOf()

    @Synchronized
    override fun addTest(testAttempt: TestAttempt) {
        this.testAttempts.add(testAttempt)
    }

    @Synchronized
    override fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
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

    @Synchronized
    override fun getTestResults(): Collection<AndroidTest> {
        val grouped: Map<TestStaticData, List<TestAttempt>> =
            testAttempts.groupBy(keySelector = { it.testResult })

        return grouped.mapValues { (_, executions) ->
            testAttemptsAggregateStrategy.getTestResult(executions)
        }.values
    }
}
