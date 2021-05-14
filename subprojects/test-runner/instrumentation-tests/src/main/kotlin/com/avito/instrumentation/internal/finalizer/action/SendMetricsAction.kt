package com.avito.instrumentation.internal.finalizer.action

import com.avito.instrumentation.internal.finalizer.verdict.Verdict
import com.avito.instrumentation.metrics.InstrumentationMetricsSender

internal class SendMetricsAction(
    private val metricsSender: InstrumentationMetricsSender
) : FinalizeAction {

    override fun action(verdict: Verdict) {
        if (verdict is Verdict.Failure) {
            if (verdict.lostTests.isNotEmpty()) {
                metricsSender.sendNotReportedCount(verdict.lostTests.size)
            }
        }
    }
}
