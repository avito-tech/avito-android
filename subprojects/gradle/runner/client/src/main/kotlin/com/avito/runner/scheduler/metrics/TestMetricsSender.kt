package com.avito.runner.scheduler.metrics

import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender

internal class TestMetricsSender(
    private val statsDSender: StatsDSender,
    private val prefix: SeriesName
) {

    fun sendInitialDelay(delayMs: Long) {
        statsDSender.send(GaugeLongMetric(prefix.append("initial-delay"), delayMs))
    }

    fun sendEndDelay(delayMs: Long) {
        statsDSender.send(GaugeLongMetric(prefix.append("end-delay"), delayMs))
    }

    fun sendMedianQueueTime(value: Long) {
        statsDSender.send(GaugeLongMetric(prefix.append("queue-median"), value))
    }

    fun sendMedianInstallationTime(value: Long) {
        statsDSender.send(GaugeLongMetric(prefix.append("install-median"), value))
    }

    fun sendSuiteTime(suiteTimeMs: Long) {
        statsDSender.send(GaugeLongMetric(prefix.append("suite"), suiteTimeMs))
    }

    fun sendTotalTime(timeMs: Long) {
        statsDSender.send(GaugeLongMetric(prefix.append("total"), timeMs))
    }

    fun sendMedianDeviceUtilization(percent: Int) {
        statsDSender.send(GaugeLongMetric(prefix.append("device-utilization-median"), percent.toLong()))
    }
}
