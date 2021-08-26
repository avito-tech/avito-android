package com.avito.runner.scheduler.metrics

import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.math.Percent
import java.time.Duration

internal class TestRunnerMetricsSender(
    private val statsDSender: StatsDSender,
    private val prefix: SeriesName
) {

    fun sendInitialDelay(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("initial-delay"), value.toMillis()))
    }

    fun sendEndDelay(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("end-delay"), value.toMillis()))
    }

    fun sendMedianQueueTime(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("queue-median"), value.toMillis()))
    }

    fun sendMedianInstallationTime(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("install-median"), value.toMillis()))
    }

    fun sendSuiteTime(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("suite"), value.toMillis()))
    }

    fun sendTotalTime(value: Duration) {
        statsDSender.send(GaugeLongMetric(prefix.append("total"), value.toMillis()))
    }

    fun sendMedianDeviceUtilization(percent: Percent) {
        statsDSender.send(GaugeLongMetric(prefix.append("device-utilization", "median"), percent.toLong()))
    }
}
