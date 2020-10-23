package com.avito.runner.service

import com.avito.logger.NoOpLogger
import com.avito.runner.logging.StdOutLogger
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.Device.DeviceStatus
import com.avito.runner.test.NoOpListener
import com.avito.runner.test.TestDispatcher
import com.avito.runner.test.generateInstalledApplicationLayer
import com.avito.runner.test.generateInstrumentationTestAction
import com.avito.runner.test.generateIntention
import com.avito.runner.test.listWithDefault
import com.avito.runner.test.mock.MockActionResult
import com.avito.runner.test.mock.MockDevice
import com.avito.runner.test.randomDeviceCoordinate
import com.avito.runner.test.receiveAvailable
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test
import java.io.File

@ExperimentalCoroutinesApi
class IntentionExecutionServiceTest {

    @Test
    fun `schedule all tests to supported devices`() =
        runBlockingTest {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val intentionsRouter = IntentionsRouter()
            val executionService = provideIntentionExecutionService(
                devices = devices,
                intentionsRouter = intentionsRouter
            )

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
                coordinate = randomDeviceCoordinate(),
                apiResult = MockActionResult.Success(22),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                gettingDeviceStatusResults = listWithDefault(
                    1 + intentions.size,
                    MockActionResult.Success<DeviceStatus>(
                        DeviceStatus.Alive
                    )
                ),
                clearPackageResults = (0 until intentions.size - 1).flatMap {
                    listOf(
                        MockActionResult.Success<Try<Any>>(
                            Try.Success(Unit)
                        ),
                        MockActionResult.Success<Try<Any>>(
                            Try.Success(Unit)
                        )
                    )
                },
                runTestsResults = intentions.map {
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    )
                }
            )

            devices.send(successfulDevice)
            val communication = executionService.start(this)
            intentions.forEach {
                communication.intentions.send(it)
            }

            val results = communication.results.receiveAvailable()
            executionService.stop()
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
    fun `reschedule test to another device - when device is broken while processing intention`() =
        runBlockingTest {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val intentionsRouter = IntentionsRouter()
            val executionService = provideIntentionExecutionService(
                devices = devices,
                intentionsRouter = intentionsRouter
            )
            val communication = executionService.start(this)

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
                )
            )
            val freezeDevice = MockDevice(
                logger = StdOutLogger(),
                coordinate = randomDeviceCoordinate(),
                apiResult = MockActionResult.Success(22),
                installApplicationResults = emptyList(),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<DeviceStatus>(
                        DeviceStatus.Alive
                    ), // State while initializing worker
                    MockActionResult.Success<DeviceStatus>(
                        DeviceStatus.Freeze(
                            RuntimeException()
                        )
                    )
                ),
                runTestsResults = emptyList()
            )
            val successfulDevice = MockDevice(
                logger = StdOutLogger(),
                coordinate = randomDeviceCoordinate(),
                apiResult = MockActionResult.Success(22),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                clearPackageResults = (0 until intentions.size - 1).flatMap {
                    listOf(
                        MockActionResult.Success<Try<Any>>(
                            Try.Success(Unit)
                        ),
                        MockActionResult.Success<Try<Any>>(
                            Try.Success(Unit)
                        )
                    )
                },
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<DeviceStatus>(
                        DeviceStatus.Alive
                    ), // State while initializing worker
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

            devices.send(freezeDevice)
            intentions.forEach { communication.intentions.send(it) }
            devices.send(successfulDevice)
            val results = communication.results.receiveAvailable()
            executionService.stop()
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
        devices: ReceiveChannel<Device>,
        intentionsRouter: IntentionsRouter
    ) = IntentionExecutionServiceImplementation(
        outputDirectory = File(""),
        logger = NoOpLogger,
        devices = devices,
        intentionsRouter = intentionsRouter,
        listener = NoOpListener,
        deviceWorkersDispatcher = TestDispatcher
    )
}
