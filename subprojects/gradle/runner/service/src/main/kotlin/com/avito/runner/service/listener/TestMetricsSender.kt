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

internal class TestMetricsSender(
    private val statsDSender: StatsDSender,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : TestListener {

    private val prefix = "runner.test.duration"

    private val logger = loggerFactory.create<TestMetricsSender>()

    private val intendedMarks = ConcurrentHashMap<Key, Long>()

    private val startedMarks = ConcurrentHashMap<Key, Long>()

    override fun intended(test: TestCase, targetPackage: String, executionNumber: Int) {
        intendedMarks[Key(test, executionNumber)] = timeProvider.nowInMillis()
    }

    override fun started(device: Device, targetPackage: String, test: TestCase, executionNumber: Int) {
        val key = Key(test, executionNumber)
        startedMarks[key] = timeProvider.nowInMillis()

        val intendedMark: Long? = intendedMarks.remove(key)

        if (intendedMark != null) {
            statsDSender.send(
                prefix = prefix,
                metric = GaugeMetric(
                    path = keyMetric("queue", key),
                    value = timeProvider.nowInMillis() - intendedMark
                )
            )
        } else {
            logger.warn("Can't send test metric: intended mark not found")
        }
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

        val startedMark = startedMarks.remove(key)

        if (startedMark != null) {
            statsDSender.send(
                prefix = prefix,
                metric = GaugeMetric(
                    path = keyMetric("execution", key),
                    value = timeProvider.nowInMillis() - startedMark
                )
            )
        } else {
            logger.warn("Can't send test metric: started mark not found")
        }
    }

    private fun keyMetric(name: String, key: Key): String =
        "${key.test.deviceName}.${key.executionNumber}.${key.test.testName}.$name"

    private data class Key(
        val test: TestCase,
        val executionNumber: Int
    )
}
