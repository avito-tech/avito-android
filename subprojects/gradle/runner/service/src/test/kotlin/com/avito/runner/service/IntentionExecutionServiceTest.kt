package com.avito.runner.service

import com.avito.runner.logging.StdOutLogger
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.observer.DevicesObserver
import com.avito.runner.test.NoOpListener
import com.avito.runner.test.generateInstalledApplicationLayer
import com.avito.runner.test.generateInstrumentationTestAction
import com.avito.runner.test.generateIntention
import com.avito.runner.test.mock.MockActionResult
import com.avito.runner.test.mock.MockDevice
import com.avito.runner.test.mock.MockDevicesObserver
import com.avito.runner.test.randomSerial
import com.avito.runner.test.randomString
import com.avito.runner.test.receiveAvailable
import com.avito.runner.test.runBlockingWithTimeout
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.delay
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.TimeUnit

class IntentionExecutionServiceTest {

    @Test
    fun `intention execution service scheduled all tests to supported devices`() =
        runBlockingWithTimeout {
            val devicesObserver = MockDevicesObserver()
            val intentionsRouter = IntentionsRouter()

            val communication = provideIntentionExecutionService(
                devicesObserver = devicesObserver,
                intentionsRouter = intentionsRouter
            ).start()

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
                api = 22,
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                gettingDeviceStatusResults = intentions.map {
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    )
                },
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
                runTestsResults = intentions.map {
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    )
                }
            )

            devicesObserver.send(successfulDevice)
            intentions.forEach { communication.intentions.send(it) }

            delay(TimeUnit.SECONDS.toMillis(2))

            val results = communication.results.receiveAvailable()

            successfulDevice.verify()

            assertWithMessage("Received results for all input intentions")
                .that(results.map { it.intention })
                .isEqualTo(intentions)

            assertWithMessage("Received only passed results from successful device")
                .that(
                    results
                        .map {
                            it.actionResult.testCaseRun.testCaseRun.result
                        }
                )
                .isEqualTo(intentions.map { TestCaseRun.Result.Passed })
        }

    @Test
    fun `intention execution service rescheduled tests because of device problems (freeze)`() =
        runBlockingWithTimeout {
            val devicesObserver = MockDevicesObserver()
            val intentionsRouter = IntentionsRouter()

            val communication = provideIntentionExecutionService(
                devicesObserver = devicesObserver,
                intentionsRouter = intentionsRouter
            ).start()

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
            val freezeDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomSerial(),
                api = 22,
                installApplicationResults = emptyList(),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Freeze(
                            RuntimeException()
                        )
                    )
                ),
                runTestsResults = emptyList()
            )
            val successfulDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomSerial(),
                api = 22,
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
                gettingDeviceStatusResults = intentions.map {
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    )
                },
                runTestsResults = intentions.map {
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    )
                }
            )

            // Add freeze device
            devicesObserver.send(freezeDevice)
            intentions.forEach { communication.intentions.send(it) }

            // Wait for device failed with infrastructure problems
            delay(TimeUnit.SECONDS.toMillis(2))

            // Add successful device
            devicesObserver.send(successfulDevice)

            // Wait for tests passed
            delay(TimeUnit.SECONDS.toMillis(2))

            val results = communication.results.receiveAvailable()

            successfulDevice.verify()

            assertWithMessage("Received results for all input intentions")
                .that(results.map { it.intention })
                // Using contains all instead of is equal because of ordering after retry first failed test
                .containsAtLeastElementsIn(intentions)

            assertWithMessage("Received only passed results from successful device")
                .that(
                    results
                        .map {
                            it.actionResult.testCaseRun.testCaseRun.result
                        }
                )
                .isEqualTo(intentions.map { TestCaseRun.Result.Passed })
        }

    private fun provideIntentionExecutionService(
        devicesObserver: DevicesObserver,
        intentionsRouter: IntentionsRouter
    ) = IntentionExecutionServiceImplementation(
        outputDirectory = File(""),
        logger = StdOutLogger(),
        devicesObserver = devicesObserver,
        intentionsRouter = intentionsRouter,
        listener = NoOpListener
    )
}
