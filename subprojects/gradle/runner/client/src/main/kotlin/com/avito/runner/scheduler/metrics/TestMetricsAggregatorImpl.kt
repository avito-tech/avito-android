package com.avito.runner.scheduler.metrics

import com.avito.math.median
import com.avito.runner.scheduler.metrics.model.TestKey
import com.avito.runner.scheduler.metrics.model.TestTimestamps

internal class TestMetricsAggregatorImpl(
    private val testSuiteStartedTime: Long,
    private val testSuiteEndedTime: Long,
    testTimestamps: Map<TestKey, TestTimestamps>
) : TestMetricsAggregator {

    private val firstTestStarted = testTimestamps.values
        .mapNotNull { it.started }
        .minOrNull()

    private val lastTestEnded = testTimestamps.values
        .mapNotNull { it.finished }
        .maxOrNull()

    private val queueTimes: List<Long> = testTimestamps.values
        .mapNotNull { it.onDevice }
        .map { it - testSuiteStartedTime }

    private val installationTimes: List<Long> = testTimestamps.values
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

    /**
     * return null if no data
     */
    private fun List<Long>.aggregateOrNull(aggregateFunc: (List<Long>) -> Number): Long? {
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
