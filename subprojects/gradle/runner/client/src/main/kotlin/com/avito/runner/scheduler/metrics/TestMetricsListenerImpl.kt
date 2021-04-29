package com.avito.runner.scheduler.metrics

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.scheduler.metrics.model.DeviceKey
import com.avito.runner.scheduler.metrics.model.DeviceTimestamps
import com.avito.runner.scheduler.metrics.model.TestKey
import com.avito.runner.scheduler.metrics.model.TestTimestamps
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.model.DeviceInstallation
import com.avito.time.TimeProvider
import java.util.concurrent.ConcurrentHashMap

internal class TestMetricsListenerImpl(
    private val testMetricsSender: TestMetricsSender,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : TestMetricsListener {

    private val logger = loggerFactory.create<TestMetricsListener>()

    private val deviceTimestamps = ConcurrentHashMap<DeviceKey, DeviceTimestamps>()

    private var testSuiteStartedTime: Long = 0

    override fun onTestSuiteStarted() {
        testSuiteStartedTime = timeProvider.nowInMillis()
    }

    override suspend fun onDeviceCreated(device: Device, state: State) {
        deviceTimestamps[device.key()] = DeviceTimestamps(
            created = timeProvider.nowInMillis(),
            testTimestamps = mutableMapOf(),
            finished = 0
        )
    }

    override suspend fun onIntentionReceived(device: Device, intention: Intention) {
        deviceTimestamps.compute(device.key()) { _: DeviceKey, oldValue: DeviceTimestamps? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.testTimestamps[intention.testKey()] = TestTimestamps(
                    onDevice = timeProvider.nowInMillis(),
                    started = null,
                    finished = null
                )
                oldValue
            }
        }
    }

    override suspend fun onStatePrepared(device: Device, state: State) {
    }

    override suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation) {
    }

    override suspend fun onTestStarted(device: Device, intention: Intention) {
        deviceTimestamps.compute(device.key()) { _: DeviceKey, oldValue: DeviceTimestamps? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.testTimestamps.compute(intention.testKey()) { _: TestKey, testTimestamps: TestTimestamps? ->
                    if (testTimestamps == null) {
                        logger.warn(
                            "Fail to set timestamp value, previous required values not found, " +
                                "this shouldn't happen"
                        )
                        null
                    } else {
                        testTimestamps.copy(started = timeProvider.nowInMillis())
                    }
                }
                oldValue
            }
        }
    }

    override suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun) {
        deviceTimestamps.compute(device.key()) { _: DeviceKey, oldValue: DeviceTimestamps? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.testTimestamps.compute(intention.testKey()) { _: TestKey, testTimestamps: TestTimestamps? ->
                    if (testTimestamps == null) {
                        logger.warn(
                            "Fail to set timestamp value, previous required values not found, " +
                                "this shouldn't happen"
                        )
                        null
                    } else {
                        testTimestamps.copy(finished = timeProvider.nowInMillis())
                    }
                }
                oldValue
            }
        }
    }

    override suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable) {
    }

    override suspend fun onDeviceDied(device: Device, message: String, reason: Throwable) {
        // alternative to onFinished terminated state
    }

    override suspend fun onFinished(device: Device) {
        deviceTimestamps.compute(device.key()) { _: DeviceKey, oldValue: DeviceTimestamps? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.copy(finished = timeProvider.nowInMillis())
            }
        }
    }

    override fun onTestSuiteFinished() {
        val aggregator: TestMetricsAggregator = createTestMetricsAggregator()

        with(testMetricsSender) {
            aggregator.initialDelay()
                ?.let { sendInitialDelay(it) }
                ?: logger.warn("Not sending initial delay, no data")

            aggregator.medianQueueTime()
                ?.let { sendMedianQueueTime(it) }
                ?: logger.warn("Not sending median test queue time, no data")

            aggregator.medianInstallationTime()
                ?.let { sendMedianInstallationTime(it) }
                ?: logger.warn("Not sending median test start time, no data")

            aggregator.endDelay()
                ?.let { sendEndDelay(it) }
                ?: logger.warn("Not sending end delay, no data")

            aggregator.suiteTime()
                ?.let { sendSuiteTime(it) }
                ?: logger.warn("Not sending suite time, no data")

            sendTotalTime(aggregator.totalTime())

            aggregator.medianDeviceUtilization()
                ?.let { sendMedianDeviceUtilization(it.toInt()) }
                ?: logger.warn("Not sending median device relative wasted time, no data. " +
                    "Aggregator value is $aggregator")
        }
    }

    private fun Device.key() = DeviceKey(coordinate.serial.value)

    private fun Intention.testKey() = TestKey(action.test, action.executionNumber)

    private fun createTestMetricsAggregator(): TestMetricsAggregator {
        return TestMetricsAggregatorImpl(
            testSuiteStartedTime = testSuiteStartedTime,
            testSuiteEndedTime = timeProvider.nowInMillis(),
            deviceTimestamps = deviceTimestamps
        )
    }
}
