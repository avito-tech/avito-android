package com.avito.runner.scheduler.metrics

import com.avito.math.median
import com.avito.runner.scheduler.metrics.model.DeviceKey
import com.avito.runner.scheduler.metrics.model.DeviceTimestamps

internal data class TestMetricsAggregatorImpl(
    private val testSuiteStartedTime: Long,
    private val testSuiteEndedTime: Long,
    private val deviceTimestamps: Map<DeviceKey, DeviceTimestamps>
) : TestMetricsAggregator {

    private val testTimestamps = deviceTimestamps.flatMap { it.value.testTimestamps.values }

    private val firstTestStarted = testTimestamps
        .mapNotNull { it.started }
        .minOrNull()

    private val lastTestEnded = testTimestamps
        .mapNotNull { it.finished }
        .maxOrNull()

    private val queueTimes: List<Long> = testTimestamps
        .mapNotNull { it.onDevice }
        .map { it - testSuiteStartedTime }

    private val installationTimes: List<Long> = testTimestamps
        .mapNotNull { it.installationTime }

    override fun initialDelay(): Long? = firstTestStarted?.let { it - testSuiteStartedTime }

    override fun endDelay(): Long? = lastTestEnded?.let { testSuiteEndedTime - it }

    override fun medianQueueTime(): Long? = queueTimes.aggregateOrNull { it.median() }

    override fun medianInstallationTime(): Long? = installationTimes.aggregateOrNull { it.median() }

    override fun suiteTime(): Long? = if (lastTestEnded != null && firstTestStarted != null) {
        lastTestEnded - firstTestStarted
    } else {
        null
    }

    override fun totalTime() = testSuiteEndedTime - testSuiteStartedTime

    override fun medianDeviceUtilization(): Long? =
        deviceTimestamps.values
            .mapNotNull { it.utilizationPercent }
            .aggregateOrNull { it.median() }

    /**
     * return null if no data
     */
    private fun List<Number>.aggregateOrNull(aggregateFunc: (List<Number>) -> Number): Long? {
        return if (isNotEmpty()) {
            val result = aggregateFunc.invoke(this).toLong()
            if (result > 0) {
                result
            } else {
                null
            }
        } else {
            null
        }
    }
}
