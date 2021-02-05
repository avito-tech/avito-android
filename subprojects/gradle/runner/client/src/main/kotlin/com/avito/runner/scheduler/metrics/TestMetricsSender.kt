package com.avito.runner.scheduler.metrics

import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.StatsDSender

internal class TestMetricsSender(
    private val statsDSender: StatsDSender,
    private val prefix: String
) {

    fun sendInitialDelay(delayMs: Long) {
        statsDSender.send(prefix, GaugeMetric("initial-delay", delayMs))
    }

    fun sendEndDelay(delayMs: Long) {
        statsDSender.send(prefix, GaugeMetric("end-delay", delayMs))
    }

    fun sendMedianQueueTime(value: Long) {
        statsDSender.send(prefix, GaugeMetric("queue-median", value))
    }

    fun sendMedianInstallationTime(value: Long) {
        statsDSender.send(prefix, GaugeMetric("install-median", value))
    }

    fun sendSuiteTime(suiteTimeMs: Long) {
        statsDSender.send(prefix, GaugeMetric("suite", suiteTimeMs))
    }

    fun sendTotalTime(timeMs: Long) {
        statsDSender.send(prefix, GaugeMetric("total", timeMs))
    }

    fun sendMedianDeviceUtilization(percent: Int) {
        statsDSender.send(prefix, GaugeMetric("device-utilization.median", percent))
    }
}
