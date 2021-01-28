package com.avito.instrumentation.metrics

import com.avito.android.stats.CountMetric
import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.StatsDSender

class InstrumentationMetricsSender(
    private val statsDSender: StatsDSender,
    buildId: String,
    instrumentationConfigName: String
) {

    private val prefix = "testrunner.$buildId.$instrumentationConfigName.tests.status"

    fun sendNotReportedCount(count: Int) {
        statsDSender.send(prefix, GaugeMetric("lost.not-reported", count))
    }

    fun sendReportFileParseErrors() {
        statsDSender.send(prefix, CountMetric("lost.parse-errors"))
    }

    fun sendReportFileNotAvailable() {
        statsDSender.send(prefix, CountMetric("lost.no-file"))
    }
}
