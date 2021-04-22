package com.avito.instrumentation.internal.finalizer.action

import com.avito.instrumentation.internal.finalizer.TestRunResult
import com.avito.instrumentation.internal.finalizer.verdict.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.Verdict
import com.avito.instrumentation.metrics.InstrumentationMetricsSender

internal class SendMetricsAction(
    private val metricsSender: InstrumentationMetricsSender
) : FinalizeAction {

    override fun action(testRunResult: TestRunResult, verdict: Verdict) {
        if (testRunResult.notReported is HasNotReportedTestsDeterminer.Result.HasNotReportedTests) {
            val lostTests = testRunResult.notReported.lostTests
            metricsSender.sendNotReportedCount(lostTests.size)
        }
    }
}
