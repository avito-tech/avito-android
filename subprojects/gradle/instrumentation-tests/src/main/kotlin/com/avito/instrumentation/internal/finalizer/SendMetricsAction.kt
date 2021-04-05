package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer.FinalizeAction
import com.avito.instrumentation.internal.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.metrics.InstrumentationMetricsSender

internal class SendMetricsAction(
    private val metricsSender: InstrumentationMetricsSender
) : FinalizeAction {

    override fun action(testRunResult: TestRunResult) {
        if (testRunResult.notReported is HasNotReportedTestsDeterminer.Result.HasNotReportedTests) {
            val lostTests = testRunResult.notReported.lostTests
            metricsSender.sendNotReportedCount(lostTests.size)
        }
    }
}
