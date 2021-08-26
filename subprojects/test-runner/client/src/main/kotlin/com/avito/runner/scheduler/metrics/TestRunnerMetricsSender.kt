package com.avito.runner.scheduler.metrics

import com.avito.math.Percent
import java.time.Duration

internal interface TestRunnerMetricsSender {
    fun sendInitialDelay(value: Duration)
    fun sendEndDelay(value: Duration)
    fun sendMedianQueueTime(value: Duration)
    fun sendMedianInstallationTime(value: Duration)
    fun sendSuiteTime(value: Duration)
    fun sendTotalTime(value: Duration)
    fun sendMedianDeviceUtilization(percent: Percent)
}
