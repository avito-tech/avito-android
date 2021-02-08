package com.avito.instrumentation.metrics

import com.avito.android.stats.CountMetric
import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender

public class InstrumentationMetricsSender(
    private val statsDSender: StatsDSender,
    runnerPrefix: SeriesName
) {

    private val prefix = runnerPrefix.append("tests", "status")

    public fun sendNotReportedCount(count: Int) {
        statsDSender.send(GaugeMetric(prefix.append("lost.not-reported"), count))
    }

    public fun sendReportFileParseErrors() {
        statsDSender.send(CountMetric(prefix.append("lost.parse-errors")))
    }

    public fun sendReportFileNotAvailable() {
        statsDSender.send(CountMetric(prefix.append("lost.no-file")))
    }
}
