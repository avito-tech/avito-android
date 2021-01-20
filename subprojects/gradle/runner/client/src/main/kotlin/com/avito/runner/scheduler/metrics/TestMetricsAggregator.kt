package com.avito.runner.scheduler.metrics

interface TestMetricsAggregator {

    fun initialDelay(): Long?

    fun endDelay(): Long?

    fun medianQueueTime(): Long?

    fun medianInstallationTime(): Long?

    fun suiteTime(): Long?

    fun totalTime(): Long
}
