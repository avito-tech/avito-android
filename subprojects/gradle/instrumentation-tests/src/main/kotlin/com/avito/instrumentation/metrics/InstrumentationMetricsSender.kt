package com.avito.instrumentation.metrics

import com.avito.android.stats.CountMetric
import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender

public class InstrumentationMetricsSender(
    private val statsDSender: StatsDSender,
    runnerPrefix: SeriesName
) {

    private val prefix = runnerPrefix.append("tests", "status")

    public fun sendNotReportedCount(count: Int) {
        statsDSender.send(GaugeLongMetric(prefix.append("lost.not-reported"), count.toLong()))
    }

    public fun sendReportFileParseErrors() {
        statsDSender.send(CountMetric(prefix.append("lost.parse-errors")))
    }

    public fun sendReportFileNotAvailable() {
        statsDSender.send(CountMetric(prefix.append("lost.no-file")))
    }
}
