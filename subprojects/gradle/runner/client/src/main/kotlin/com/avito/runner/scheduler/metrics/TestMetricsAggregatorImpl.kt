package com.avito.runner.scheduler.metrics

import com.avito.android.Result
import com.avito.math.median
import com.avito.runner.scheduler.metrics.model.DeviceKey
import com.avito.runner.scheduler.metrics.model.DeviceTimestamps
import com.avito.runner.scheduler.metrics.model.TestTimestamps

internal data class TestMetricsAggregatorImpl(
    private val testSuiteStartedTime: Long,
    private val testSuiteEndedTime: Long,
    private val deviceTimestamps: Map<DeviceKey, DeviceTimestamps>
) : TestMetricsAggregator {

    private val testTimestamps = deviceTimestamps.flatMap { it.value.testTimestamps.values }

    private val firstTestStarted: Result<Long> = testTimestamps.filterIsInstance<TestTimestamps.Finished>()
        .map { it.startTime }
        .minOrNull()
        .toResult()

    private val lastTestEnded: Result<Long> = testTimestamps.filterIsInstance<TestTimestamps.Finished>()
        .map { it.finishTime }
        .maxOrNull()
        .toResult()

    private val queueTimes: List<Long> = testTimestamps.filterIsInstance<TestTimestamps.Finished>()
        .map { it.onDevice - testSuiteStartedTime }

    private val installationTimes: List<Long> = testTimestamps.filterIsInstance<TestTimestamps.Finished>()
        .map { it.installationTime }

    override fun initialDelay(): Result<Long> = firstTestStarted.map { it - testSuiteStartedTime }

    override fun endDelay(): Result<Long> = lastTestEnded.map { testSuiteEndedTime - it }

    override fun medianQueueTime(): Result<Long> = queueTimes.aggregateOrNull { it.median() }

    override fun medianInstallationTime(): Result<Long> = installationTimes.aggregateOrNull { it.median() }

    override fun suiteTime(): Result<Long> = lastTestEnded.combine(firstTestStarted) { last, first -> last - first }

    override fun totalTime() = testSuiteEndedTime - testSuiteStartedTime

    override fun medianDeviceUtilization(): Result<Long> =
        deviceTimestamps.values.filterIsInstance<DeviceTimestamps.Finished>()
            .map { it.utilizationPercent }
            .aggregateOrNull { it.median() }

    private fun Long?.toResult(): Result<Long> = when (this) {
        null -> noDataResult()
        else -> Result.Success(this)
    }

    private fun List<Number>.aggregateOrNull(aggregateFunc: (List<Number>) -> Number): Result<Long> {
        if (isEmpty()) return noDataResult()
        val result = aggregateFunc(this).toLong()
        return when {
            result > 0 -> Result.Success(result)
            else -> noDataResult()
        }
    }

    // TODO: err message
    private fun noDataResult() = Result.Failure<Long>(IllegalStateException("There is no data to collect"))
}
