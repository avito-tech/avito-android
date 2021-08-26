package com.avito.runner.scheduler.metrics

import com.avito.android.Result
import com.avito.math.Percent
import java.time.Duration

internal interface TestRunnerMetricsProvider {

    fun initialDelay(): Result<Duration>

    fun endDelay(): Result<Duration>

    fun medianQueueTime(): Result<Duration>

    fun medianInstallationTime(): Result<Duration>

    fun suiteTime(): Result<Duration>

    fun totalTime(): Duration

    fun medianDeviceUtilization(): Result<Percent>
}
