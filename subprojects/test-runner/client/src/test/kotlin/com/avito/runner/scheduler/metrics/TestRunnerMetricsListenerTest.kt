package com.avito.runner.scheduler.metrics

import com.avito.logger.PrintlnLoggerFactory
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.createStubInstance
import com.avito.runner.service.model.intention.Intention
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.model.intention.createStubInstance
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.stub.StubDevice
import com.avito.time.DefaultTimeProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TestRunnerMetricsListenerTest {
    private val listener = TestRunnerMetricsListener(
        testMetricsSender = StubTestMetricsSender(),
        timeProvider = DefaultTimeProvider(),
        loggerFactory = PrintlnLoggerFactory
    )

    @Test
    fun `add one test - success`() {
        val device = StubDevice(loggerFactory = PrintlnLoggerFactory)
        val state = State(emptyList())
        val intention = Intention.createStubInstance(state)
        val result = DeviceTestCaseRun.createStubInstance()

        runBlocking {
            listener.onDeviceCreated(device, state)
            listener.onIntentionReceived(device, intention)
            listener.onTestStarted(device, intention)
            listener.onTestCompleted(device, intention, result)
            listener.onFinished(device)
        }
    }

    @Test
    fun `first device start and second finish - IllegaStateException`() {
        val deviceFirst = StubDevice(
            coordinate = DeviceCoordinate.Local(Serial.Local("first")),
            loggerFactory = PrintlnLoggerFactory)
        val deviceSecond = StubDevice(
            coordinate = DeviceCoordinate.Local(Serial.Local("second")),
            loggerFactory = PrintlnLoggerFactory
        )
        val state = State(emptyList())

        val error = assertThrows<IllegalStateException> {
            runBlocking {
                listener.onDeviceCreated(deviceFirst, state)
                listener.onFinished(deviceSecond)
            }
        }

        assertThat(error.message)
            .isEqualTo("Can't find DeviceWorkerState for Local(serial=second)")
    }

    @Test
    fun `onDeviceCreated twice - IllegalStateException`() {
        val device = StubDevice(loggerFactory = PrintlnLoggerFactory)
        val state = State(emptyList())

        val error = assertThrows<IllegalStateException> {
            runBlocking {
                listener.onDeviceCreated(device, state)
                listener.onDeviceCreated(device, state)
            }
        }

        assertThat(error.message)
            .isEqualTo("Device Local(serial=stub) already called onDeviceCreated")
    }

    @Test
    fun `onFinished called before onDeviceCreated - IllegalStateException`() {
        val device = StubDevice(loggerFactory = PrintlnLoggerFactory)

        val error = assertThrows<IllegalStateException> {
            runBlocking {
                listener.onFinished(device)
            }
        }

        assertThat(error.message)
            .isEqualTo("Can't find DeviceWorkerState for Local(serial=stub)")
    }

    @Test
    fun `onIntentionReceived called before onDeviceCreated - IllegalStateException`() {
        val device = StubDevice(loggerFactory = PrintlnLoggerFactory)
        val state = State(emptyList())

        val error = assertThrows<IllegalStateException> {
            runBlocking {
                listener.onIntentionReceived(device, Intention.createStubInstance(state))
            }
        }

        assertThat(error.message)
            .isEqualTo("Can't find DeviceWorkerState for Local(serial=stub)")
    }

    @Test
    fun `onTestStarted called before onDeviceCreated - IllegalStateException`() {
        val device = StubDevice(loggerFactory = PrintlnLoggerFactory)
        val state = State(emptyList())

        val error = assertThrows<IllegalStateException> {
            runBlocking {
                listener.onTestStarted(device, Intention.createStubInstance(state))
            }
        }

        assertThat(error.message)
            .isEqualTo("Can't find DeviceWorkerState for Local(serial=stub)")
    }

    @Test
    fun `onTestCompleted called before onDeviceCreated - IllegalStateException`() {
        val device = StubDevice(loggerFactory = PrintlnLoggerFactory)
        val state = State(emptyList())

        val error = assertThrows<IllegalStateException> {
            runBlocking {
                listener.onTestCompleted(
                    device,
                    Intention.createStubInstance(state),
                    DeviceTestCaseRun.createStubInstance()
                )
            }
        }

        assertThat(error.message)
            .isEqualTo("Can't find DeviceWorkerState for Local(serial=stub)")
    }
}
