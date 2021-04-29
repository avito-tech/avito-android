package com.avito.android.runner.report.internal

import com.avito.android.runner.report.Report
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData
import com.avito.time.TimeProvider

internal class InMemoryReport(private val timeProvider: TimeProvider) : Report {

    private val testAttempts = mutableListOf<AndroidTest>()

    @Synchronized
    override fun addTest(test: AndroidTest) {
        this.testAttempts.add(test)
    }

    @Synchronized
    override fun addSkippedTests(skippedTests: List<Pair<TestStaticData, String>>) {
        this.testAttempts.addAll(
            skippedTests.map { (test, reason) ->
                AndroidTest.Skipped.fromTestMetadata(
                    testStaticData = test,
                    skipReason = reason,
                    reportTime = timeProvider.nowInSeconds()
                )
            }
        )
    }

    @Synchronized
    override fun getTests(): List<AndroidTest> {
        return testAttempts
    }
}
