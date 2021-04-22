package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status

internal class LegacyTestStatisticsCounter(
    private val reportedTests: List<SimpleRunTest>,
    val failed: HasFailedTestDeterminer.Result,
    val notReported: HasNotReportedTestsDeterminer.Result
) : TestStatisticsCounter {

    override fun overallDuration(): Int {
        return reportedTests.sumBy { it.lastAttemptDurationInSeconds }
    }

    override fun overallCount(): Int {
        return reportedTests.size + notReported.lostTests.size
    }

    override fun successCount(): Int {
        return reportedTests.filter { it.status is Status.Success || it.status is Status.Manual }.size
    }

    override fun skippedCount(): Int {
        return reportedTests.filter { it.status is Status.Skipped }.size
    }

    override fun failureCount(): Int {
        return failed.count()
    }

    override fun notReportedCount(): Int {
        return notReported.lostTests.size
    }
}
