package com.avito.runner.service

import com.avito.runner.logging.StdOutLogger
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.DeviceWorker
import com.avito.runner.service.worker.DeviceWorkerMessage
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.Device.DeviceStatus
import com.avito.runner.test.NoOpListener
import com.avito.runner.test.generateInstalledApplicationLayer
import com.avito.runner.test.generateInstrumentationTestAction
import com.avito.runner.test.generateIntention
import com.avito.runner.test.listWithDefault
import com.avito.runner.test.mock.MockActionResult
import com.avito.runner.test.mock.MockDevice
import com.avito.runner.test.randomSerial
import com.avito.runner.test.receiveAvailable
import com.avito.runner.test.runBlockingWithTimeout
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.TimeUnit

class DeviceWorkerTest {

    @Test
    fun `returns application installed event and 4 passed intentions for 4 tests with of 2 applications`() =
        runBlockingWithTimeout {
            val compatibleWithDeviceState = State(
                layers = listOf(
                    State.Layer.Model(model = "model"),
                    State.Layer.ApiLevel(api = 22),
                    generateInstalledApplicationLayer(),
                    generateInstalledApplicationLayer()
                )
            )
            val intentions = listOf(
                generateIntention(
                    state = compatibleWithDeviceState,
                    action = generateInstrumentationTestAction()
                ),
                generateIntention(
                    state = compatibleWithDeviceState,
                    action = generateInstrumentationTestAction()
                ),
                generateIntention(
                    state = compatibleWithDeviceState,
                    action = generateInstrumentationTestAction()
                ),
                generateIntention(
                    state = compatibleWithDeviceState,
                    action = generateInstrumentationTestAction()
                )
            )
            val successfulDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomSerial(),
                apiResult = MockActionResult.Success(22),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                clearPackageResults = (0 until intentions.size - 1).flatMap {
                    listOf(
                        MockActionResult.Success<Try<Any>>(
                            Try.Success(
                                Unit
                            )
                        ),
                        MockActionResult.Success<Try<Any>>(
                            Try.Success(
                                Unit
                            )
                        )
                    )
                },
                gettingDeviceStatusResults = listWithDefault(
                    1 + intentions.size,
                    MockActionResult.Success<DeviceStatus>(
                        DeviceStatus.Alive
                    )
                ),
                runTestsResults = intentions.map {
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    )
                }
            )

            val resultsChannel: Channel<DeviceWorkerMessage> =
                Channel(Channel.UNLIMITED)

            val router = IntentionsRouter().apply {
                intentions.forEach { sendIntention(it) }
            }

            val worker = provideDeviceWorker(
                results = resultsChannel,
                device = successfulDevice,
                router = router
            ).run()

            // Wait before all intentions will be processed
            delay(TimeUnit.SECONDS.toMillis(3))
            router.close()
            worker.join()

            val results = resultsChannel.receiveAvailable()

            successfulDevice.verify()

            assertWithMessage("Installed two applications")
                .that(results.filterIsInstance<DeviceWorkerMessage.ApplicationInstalled>())
                .hasSize(2)

            assertWithMessage("Received results for all sent intentions")
                .that(
                    results
                        .filterIsInstance<DeviceWorkerMessage.Result>()
                        .map { it.intentionResult.intention }
                )
                .isEqualTo(intentions)
        }

    @Test
    fun `fail with no events - device is freeze before processing intentions`() =
        runBlockingWithTimeout {
            val freezeDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomSerial(),
                apiResult = MockActionResult.Success(22),
                installApplicationResults = emptyList(),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<DeviceStatus>(
                        DeviceStatus.Freeze(
                            RuntimeException()
                        )
                    )
                ),
                runTestsResults = emptyList()
            )

            val resultsChannel = Channel<DeviceWorkerMessage>(Channel.UNLIMITED)
            val router = IntentionsRouter()

            val worker = provideDeviceWorker(
                results = resultsChannel,
                device = freezeDevice,
                router = router
            ).run()

            // Wait before all intentions will be processed
            delay(TimeUnit.SECONDS.toMillis(3))
            router.close()
            worker.join()

            freezeDevice.verify()

            assertWithMessage("Received no messages")
                .that(resultsChannel.isEmpty)
                .isTrue()
        }

    @Test
    fun `returns failed message  - device is freeze while processing first intention`() =
        runBlockingWithTimeout {
            val compatibleWithDeviceState = State(
                layers = listOf(
                    State.Layer.Model(model = "model"),
                    State.Layer.ApiLevel(api = 22),
                    generateInstalledApplicationLayer(),
                    generateInstalledApplicationLayer()
                )
            )
            val intentions = listOf(
                generateIntention(
                    state = compatibleWithDeviceState,
                    action = generateInstrumentationTestAction()
                ),
                generateIntention(
                    state = compatibleWithDeviceState,
                    action = generateInstrumentationTestAction()
                )
            )
            val freezeDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomSerial(),
                apiResult = MockActionResult.Success(22),
                installApplicationResults = emptyList(),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<DeviceStatus>(
                        DeviceStatus.Alive
                    ),
                    MockActionResult.Success<DeviceStatus>(
                        DeviceStatus.Freeze(
                            RuntimeException()
                        )
                    )
                ),
                runTestsResults = emptyList()
            )

            val resultsChannel: Channel<DeviceWorkerMessage> =
                Channel(Channel.UNLIMITED)
            val router = IntentionsRouter().apply {
                intentions.forEach { sendIntention(it) }
            }

            val worker = provideDeviceWorker(
                results = resultsChannel,
                device = freezeDevice,
                router = router
            ).run()

            // Wait before all intentions will be processed
            delay(TimeUnit.SECONDS.toMillis(3))
            router.close()
            worker.join()

            val results = resultsChannel.receiveAvailable()

            freezeDevice.verify()

            assertWithMessage("Received only one message from worker")
                .that(results)
                .hasSize(1)

            assertWithMessage("Received only Worker failed message for freeze device")
                .that(results[0])
                .isInstanceOf(DeviceWorkerMessage.WorkerFailed::class.java)
        }

    private fun provideDeviceWorker(
        results: Channel<DeviceWorkerMessage>,
        device: Device,
        router: IntentionsRouter
    ) = DeviceWorker(
        intentionsRouter = router,
        messagesChannel = results,
        device = device,
        outputDirectory = File(""),
        listener = NoOpListener
    )
}
