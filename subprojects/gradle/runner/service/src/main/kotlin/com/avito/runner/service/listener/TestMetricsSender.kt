package com.avito.runner.service.listener

import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.StatsDSender
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import com.avito.time.TimeProvider
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

class TestMetricsSender(
    private val statsDSender: StatsDSender,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : TestListener {

    private val logger = loggerFactory.create<TestMetricsSender>()

    private val prefix = "runner.test.duration"

    private val onDeviceMarks = ConcurrentHashMap<Key, Long>()

    private val startedMarks = ConcurrentHashMap<Key, Long>()

    private var testSuiteStartedTime: Long = 0

    fun onTestSuiteStarted() {
        testSuiteStartedTime = timeProvider.nowInMillis()
    }

    override fun onDevice(
        device: Device,
        test: TestCase,
        targetPackage: String,
        executionNumber: Int
    ) {
        val key = Key(test, executionNumber)
        onDeviceMarks[key] = timeProvider.nowInMillis()
    }

    override fun started(
        device: Device,
        targetPackage: String,
        test: TestCase,
        executionNumber: Int
    ) {
        val key = Key(test, executionNumber)
        startedMarks[key] = timeProvider.nowInMillis()
    }

    override fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int
    ) {
        val key = Key(test, executionNumber)
    }

    fun onTestSuiteFinished() {
        val startToOnDeviceDeltas = onDeviceMarks.values.map { it - testSuiteStartedTime }
        statsDSender.send(prefix, GaugeMetric("queue-average", startToOnDeviceDeltas.average().roundToInt()))

        val onDeviceToStartedDeltas = startedMarks.map { (key, value) ->
            val onDeviceMark = onDeviceMarks[key]
            if (onDeviceMark == null) {
                logger.warn("No onDevice event for $key")
                null
            } else {
                value - onDeviceMark
            }
        }.filterNotNull()

        statsDSender.send(prefix, GaugeMetric("start-average", onDeviceToStartedDeltas.average().roundToInt()))
    }

    private data class Key(
        val test: TestCase,
        val executionNumber: Int
    )
}
