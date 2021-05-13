package com.avito.instrumentation.internal.finalizer.action

import com.avito.instrumentation.internal.finalizer.TestRunResult
import com.avito.instrumentation.internal.finalizer.verdict.LegacyVerdict
import com.avito.instrumentation.metrics.InstrumentationMetricsSender

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
