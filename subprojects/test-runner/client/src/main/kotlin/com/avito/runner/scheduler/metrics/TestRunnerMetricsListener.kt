package com.avito.runner.scheduler.metrics

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.scheduler.metrics.model.DeviceKey
import com.avito.runner.scheduler.metrics.model.DeviceWorkerEvents
import com.avito.runner.scheduler.metrics.model.TestExecutionEvent
import com.avito.runner.scheduler.metrics.model.TestKey
import com.avito.runner.scheduler.metrics.model.finish
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.listener.DeviceListener
import com.avito.runner.service.worker.model.DeviceInstallation
import com.avito.time.TimeProvider
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

internal class TestRunnerMetricsListener(
    private val testMetricsSender: TestRunnerMetricsSender,
    private val timeProvider: TimeProvider,
    loggerFactory: LoggerFactory
) : TestSuiteListener, DeviceListener {

    private val logger = loggerFactory.create<TestRunnerMetricsListener>()

    private val deviceWorkerEvents = ConcurrentHashMap<DeviceKey, DeviceWorkerEvents>()

    private var testSuiteStartedTime: Duration = Duration.ofMillis(0)

    override fun onTestSuiteStarted() {
        testSuiteStartedTime = timeProvider.nowInDuration()
    }

    override suspend fun onDeviceCreated(device: Device, state: State) {
        deviceWorkerEvents[device.key()] = DeviceWorkerEvents.Created(
            created = timeProvider.nowInDuration(),
            testExecutionEvents = mutableMapOf(),
        )
    }

    override suspend fun onIntentionReceived(device: Device, intention: Intention) {
        deviceWorkerEvents.compute(device.key()) { _: DeviceKey, oldValue: DeviceWorkerEvents? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.testExecutionEvents[intention.testKey()] = TestExecutionEvent.IntentionReceived(
                    timeProvider.nowInDuration()
                )
                oldValue
            }
        }
    }

    override suspend fun onStatePrepared(device: Device, state: State) {
        // empty
    }

    override suspend fun onApplicationInstalled(device: Device, installation: DeviceInstallation) {
        // empty
    }

    override suspend fun onTestStarted(device: Device, intention: Intention) {
        deviceWorkerEvents.compute(device.key()) { _: DeviceKey, oldValue: DeviceWorkerEvents? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.testExecutionEvents.compute(intention.testKey()) { _: TestKey, events: TestExecutionEvent? ->
                    if (events == null) {
                        logger.warn(
                            "Fail to set timestamp value, previous required values not found, " +
                                "this shouldn't happen"
                        )
                        null
                    } else {
                        events.start(timeProvider.nowInDuration())
                    }
                }
                oldValue
            }
        }
    }

    override suspend fun onTestCompleted(device: Device, intention: Intention, result: DeviceTestCaseRun) {
        deviceWorkerEvents.compute(device.key()) { _: DeviceKey, oldValue: DeviceWorkerEvents? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.testExecutionEvents.compute(intention.testKey()) { _: TestKey, events: TestExecutionEvent? ->
                    if (events == null) {
                        logger.warn(
                            "Fail to set timestamp value, previous required values not found, " +
                                "this shouldn't happen"
                        )
                        null
                    } else {
                        events.finish(timeProvider.nowInDuration())
                    }
                }
                oldValue
            }
        }
    }

    override suspend fun onIntentionFail(device: Device, intention: Intention, reason: Throwable) {
        // TODO Should we handle this as a test complete?
    }

    override suspend fun onDeviceDied(device: Device, message: String, reason: Throwable) {
        // alternative to onFinished terminated state
    }

    override suspend fun onFinished(device: Device) {
        deviceWorkerEvents.compute(device.key()) { _: DeviceKey, oldValue: DeviceWorkerEvents? ->
            if (oldValue == null) {
                logger.warn("Fail to set timestamp value, previous required values not found, this shouldn't happen")
                null
            } else {
                oldValue.finish(finished = timeProvider.nowInDuration())
            }
        }
    }

    override fun onTestSuiteFinished() {
        val provider = createMetricsProvider()

        with(testMetricsSender) {
            provider.initialDelay().fold(
                { sendInitialDelay(it) },
                { logger.warn("Not sending initial delay, no data") }
            )

            provider.medianQueueTime().fold(
                { sendMedianQueueTime(it) },
                { logger.warn("Not sending median test queue time, no data") }
            )
            provider.medianInstallationTime().fold(
                { sendMedianInstallationTime(it) },
                { logger.warn("Not sending median test start time, no data") }
            )

            provider.endDelay().fold(
                { sendEndDelay(it) },
                { logger.warn("Not sending end delay, no data") }
            )

            provider.suiteTime().fold(
                { sendSuiteTime(it) },
                { logger.warn("Not sending suite time, no data") }
            )

            sendTotalTime(provider.totalTime())

            provider.medianDeviceUtilization().fold(
                { sendMedianDeviceUtilization(it) },
                { logger.warn("Not sending median device relative wasted time, no data") }
            )
        }
    }

    private fun Device.key() = DeviceKey(coordinate.serial.value)

    private fun Intention.testKey() = TestKey(action.test, action.executionNumber)

    private fun createMetricsProvider(): TestRunnerMetricsProvider {
        return TestRunnerMetricsProviderImpl(
            testSuiteStartedTime = testSuiteStartedTime,
            testSuiteEndedTime = timeProvider.nowInDuration(),
            deviceWorkerEvents = deviceWorkerEvents
        )
    }
}
