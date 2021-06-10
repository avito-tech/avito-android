package com.avito.runner.finalizer.verdict

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.duration

internal class TestStatisticsCounterImpl(private val verdict: Verdict) : TestStatisticsCounter {

    override fun overallDurationSec(): Int =
        verdict.testResults
            .filterIsInstance<TestRuntimeData>()
            .map { it.duration }
            .sum()
            .toInt()

    override fun overallCount(): Int = verdict.testResults.size

    override fun successCount(): Int = completedTests().filter { it.incident == null }.size

    override fun skippedCount(): Int = verdict.testResults.filterIsInstance<AndroidTest.Skipped>().size

    override fun failureCount(): Int =
        if (verdict is Verdict.Failure) verdict.unsuppressedFailedTests.size else 0

    override fun notReportedCount(): Int =
        if (verdict is Verdict.Failure) verdict.notReportedTests.size else 0

    private fun completedTests(): List<AndroidTest.Completed> {
        return verdict.testResults.filterIsInstance<AndroidTest.Completed>()
    }
}
