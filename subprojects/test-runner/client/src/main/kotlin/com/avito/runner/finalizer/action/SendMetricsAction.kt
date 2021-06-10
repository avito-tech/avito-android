package com.avito.runner.finalizer.action

import com.avito.runner.finalizer.verdict.Verdict
import com.avito.runner.scheduler.metrics.InstrumentationMetricsSender

internal class SendMetricsAction(
    private val metricsSender: InstrumentationMetricsSender
) : FinalizeAction {

    override fun action(verdict: Verdict) {
        if (verdict is Verdict.Failure) {
            if (verdict.notReportedTests.isNotEmpty()) {
                metricsSender.sendNotReportedCount(verdict.notReportedTests.size)
            }
        }
    }
}
