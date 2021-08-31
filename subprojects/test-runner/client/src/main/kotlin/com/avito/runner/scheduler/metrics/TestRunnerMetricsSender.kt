package com.avito.runner.scheduler.metrics

import java.time.Duration

internal interface TestRunnerMetricsSender {
    fun sendInitialDelay(value: Duration)
    fun sendEndDelay(value: Duration)
    fun sendMedianQueueTime(value: Duration)
    fun sendMedianInstallationTime(value: Duration)
    fun sendSuiteTime(value: Duration)
    fun sendTotalTime(value: Duration)
    fun sendDevicesLiving(value: Duration)
    fun sendDevicesWorking(value: Duration)
    fun sendDevicesIdle(value: Duration)
}
