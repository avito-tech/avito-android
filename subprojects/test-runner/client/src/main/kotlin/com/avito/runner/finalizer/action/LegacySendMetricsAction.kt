package com.avito.runner.finalizer.action

import com.avito.runner.finalizer.TestRunResult
import com.avito.runner.finalizer.verdict.LegacyVerdict
import com.avito.runner.scheduler.metrics.InstrumentationMetricsSender

internal class LegacySendMetricsAction(
    private val metricsSender: InstrumentationMetricsSender
) : LegacyFinalizeAction {

    override fun action(testRunResult: TestRunResult, verdict: LegacyVerdict) {
        if (verdict is LegacyVerdict.Failure) {
            val lostTests = verdict.prettifiedDetails.lostTests
            if (lostTests.isNotEmpty()) {
                metricsSender.sendNotReportedCount(lostTests.size)
            }
        }
    }
}
