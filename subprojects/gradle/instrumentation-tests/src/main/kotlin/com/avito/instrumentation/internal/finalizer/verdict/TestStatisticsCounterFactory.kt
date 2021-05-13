package com.avito.instrumentation.internal.finalizer.verdict

import com.avito.report.model.SimpleRunTest

internal object TestStatisticsCounterFactory {

    fun createLegacy(
        reportedTests: List<SimpleRunTest>,
        failedTestDeterminer: HasFailedTestDeterminer.Result,
        notReportedTestsDeterminer: HasNotReportedTestsDeterminer.Result
    ): TestStatisticsCounter = LegacyTestStatisticsCounter(
        reportedTests = reportedTests,
        failed = failedTestDeterminer,
        notReported = notReportedTestsDeterminer
    )

    fun create(verdict: Verdict): TestStatisticsCounter = TestStatisticsCounterImpl(verdict)
}
