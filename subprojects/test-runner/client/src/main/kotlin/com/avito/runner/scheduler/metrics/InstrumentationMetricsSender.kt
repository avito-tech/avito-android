package com.avito.runner.scheduler.metrics

import com.avito.android.stats.CountMetric
import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.StatsDSender
import com.avito.graphite.series.SeriesName

public class InstrumentationMetricsSender(
    private val statsDSender: StatsDSender,
    runnerPrefix: SeriesName
) {

    private val prefix = runnerPrefix.append("tests", "status")

    public fun sendNotReportedCount(count: Int) {
        statsDSender.send(GaugeLongMetric(prefix.append("lost.not-reported"), count.toLong()))
    }

    public fun sendReportFileNotAvailable() {
        statsDSender.send(CountMetric(prefix.append("lost.no-file")))
    }

    public fun sendUnexpectedInfraError() {
        statsDSender.send(CountMetric(prefix.append("lost.instr-unexpected")))
    }

    public fun sendFailedOnParsingInstrumentation() {
        statsDSender.send(CountMetric(prefix.append("lost.instr-parsing")))
    }

    public fun sendFailedOnStartInstrumentation() {
        statsDSender.send(CountMetric(prefix.append("lost.instr-start")))
    }

    public fun sendTimeOut() {
        statsDSender.send(CountMetric(prefix.append("lost.instr-timeout")))
    }
}
