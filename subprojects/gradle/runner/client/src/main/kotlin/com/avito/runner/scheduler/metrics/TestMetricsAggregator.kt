package com.avito.runner.scheduler.metrics

import com.avito.android.Result

internal interface TestMetricsAggregator {

    fun initialDelay(): Result<Long>

    fun endDelay(): Result<Long>

    fun medianQueueTime(): Result<Long>

    fun medianInstallationTime(): Result<Long>

    fun suiteTime(): Result<Long>

    fun totalTime(): Long

    fun medianDeviceUtilization(): Result<Long>
}
