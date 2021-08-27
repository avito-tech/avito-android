package com.avito.runner.scheduler.metrics

import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.math.Percent
import java.time.Duration

internal class TestRunnerMetricsSenderImpl(
    private val statsDSender: StatsDSender,
    private val prefix: SeriesName
) : TestRunnerMetricsSender {

    override fun sendInitialDelay(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("initial-delay"), value.toMillis()))
    }

    override fun sendEndDelay(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("end-delay"), value.toMillis()))
    }

    override fun sendMedianQueueTime(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("queue-median"), value.toMillis()))
    }

    override fun sendMedianInstallationTime(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("install-median"), value.toMillis()))
    }

    override fun sendSuiteTime(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("suite"), value.toMillis()))
    }

    override fun sendTotalTime(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("total"), value.toMillis()))
    }

    override fun sendMedianDeviceUtilization(percent: Percent) {
        statsDSender.send(GaugeLongMetric(prefix.append("device-utilization", "median"), percent.toLong()))
    }
}
