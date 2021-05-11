package com.avito.runner.scheduler.metrics

internal interface TestMetricsAggregator {

    fun initialDelay(): Long?

    fun endDelay(): Long?

    fun medianQueueTime(): Long?

    fun medianInstallationTime(): Long?

    fun suiteTime(): Long?

    fun totalTime(): Long

    fun medianDeviceUtilization(): Long?
}
