package com.avito.runner.scheduler.metrics

import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.StatsDSender

internal class TestMetricsSender(
    private val statsDSender: StatsDSender,
    buildId: String,
    instrumentationConfigName: String
) {

    private val prefix = "testrunner.$buildId.$instrumentationConfigName"

    /**
     * from instrumentation task start to first test execution started
     */
    fun sendInitialDelay(delayMs: Long) {
        statsDSender.send(prefix, GaugeMetric("initial-delay", delayMs))
    }

    /**
     * from last test execution finished to instrumentation task finish
     */
    fun sendEndDelay(delayMs: Long) {
        statsDSender.send(prefix, GaugeMetric("end-delay", delayMs))
    }

    /**
     * average from task start to device claimed by test
     */
    fun sendAverageTestQueueTime(averageTime: Long) {
        statsDSender.send(prefix, GaugeMetric("queue-average", averageTime))
    }

    /**
     * average from on device to test execution started
     */
    fun sendAverageTestStartTime(averageTime: Long) {
        statsDSender.send(prefix, GaugeMetric("start-average", averageTime))
    }

    /**
     * whole test suite time, including retries
     */
    fun sendSuiteTime(suiteTimeMs: Long) {
        statsDSender.send(prefix, GaugeMetric("suite", suiteTimeMs))
    }

    /**
     * total job time
     */
    fun sendTotalTime(timeMs: Long) {
        statsDSender.send(prefix, GaugeMetric("total", timeMs))
    }
}
