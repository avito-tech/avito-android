package com.avito.runner.scheduler.metrics

import com.avito.logger.LoggerFactory
import com.avito.logger.create

internal class TestMetricsComputer(
    loggerFactory: LoggerFactory,
    private val testSuiteStartedTime: Long,
    private val testSuiteEndedTime: Long,
    private val onDeviceMarks: Map<TestKey, Long>,
    startedMarks: Map<TestKey, Long>,
    finishedMarks: Map<TestKey, Long>
) {

    private val logger = loggerFactory.create<TestMetricsComputer>()

    private val firstTestStarted = startedMarks.values.minOrNull()

    private val lastTestEnded = finishedMarks.values.maxOrNull()

    private val queueTimes: List<Long> = onDeviceMarks.map { it.value - testSuiteStartedTime }

    private val startTimes: List<Long> = startedMarks
        .map { (key, value) ->
            val onDeviceMark = onDeviceMarks[key]
            if (onDeviceMark == null) {
                logger.warn("No onDevice event for $key")
                null
            } else {
                value - onDeviceMark
            }
        }
        .filterNotNull()

    /**
     * null is possible if no tests
     */
    fun initialDelay(): Long? = firstTestStarted?.let { it - testSuiteStartedTime }

    fun endDelay(): Long? = lastTestEnded?.let { testSuiteEndedTime - it }

    fun averageTestQueueTime(): Long? = queueTimes.aggregateOrNull { it.average() }

    fun averageTestStartTime(): Long? = startTimes.aggregateOrNull { it.average() }

    fun suiteTime(): Long? = if (lastTestEnded != null && firstTestStarted != null) {
        lastTestEnded - firstTestStarted
    } else {
        null
    }

    fun totalTime() = testSuiteEndedTime - testSuiteStartedTime

    /**
     * return null if no data
     */
    private fun List<Long>.aggregateOrNull(aggregateFunc: (List<Long>) -> Number): Long? {
        return if (isNotEmpty()) {
            val result = aggregateFunc.invoke(queueTimes).toLong()
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
