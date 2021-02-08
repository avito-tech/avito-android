package com.avito.instrumentation.metrics

import com.avito.android.stats.CountMetric
import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.StatsDSender

public class InstrumentationMetricsSender(
    private val statsDSender: StatsDSender,
    buildId: String,
    instrumentationConfigName: String
) {

    private val prefix = "testrunner.$buildId.$instrumentationConfigName.tests.status"

    public fun sendNotReportedCount(count: Int) {
        statsDSender.send(prefix, GaugeMetric("lost.not-reported", count))
    }

    public fun sendReportFileParseErrors() {
        statsDSender.send(prefix, CountMetric("lost.parse-errors"))
    }

    public fun sendReportFileNotAvailable() {
        statsDSender.send(prefix, CountMetric("lost.no-file"))
    }
}
