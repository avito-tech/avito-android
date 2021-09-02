package com.avito.runner.scheduler.metrics

import com.avito.android.Result
import java.time.Duration

internal interface TestRunnerMetricsProvider {

    fun initialDelay(): Result<Duration>

    fun endDelay(): Result<Duration>

    fun medianQueueTime(): Result<Duration>

    fun medianInstallationTime(): Result<Duration>

    fun suiteTime(): Result<Duration>

    fun totalTime(): Duration

    fun devicesLiving(): Duration

    fun devicesWorking(): Duration

    fun devicesIdle(): Duration
}
