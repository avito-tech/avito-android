package com.avito.runner.service

import com.avito.logger.NoOpLogger
import com.avito.runner.logging.StdOutLogger
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.DeviceWorker
import com.avito.runner.service.worker.DeviceWorkerMessage
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
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runBlockingTest
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test
import java.io.File

@ExperimentalCoroutinesApi
class DeviceWorkerTest {

    val logger = NoOpLogger

    @Test
    fun `returns application installed event and 4 passed intentions for 4 tests with of 2 applications`() =
        runBlockingTest {
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

            val router = IntentionsRouter(logger = logger).apply {
                intentions.forEach { sendIntention(it) }
            }

            val worker = provideDeviceWorker(
                results = resultsChannel,
                device = successfulDevice,
                router = router
            ).run(this)

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
    fun `fail with device died event - device is freeze before processing intentions`() =
        runBlockingTest {
            val freezeDevice = MockDevice(
                logger = StdOutLogger(),
                coordinate = randomDeviceCoordinate(),
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
            val router = IntentionsRouter(logger = logger)

            val worker = provideDeviceWorker(
                results = resultsChannel,
                device = freezeDevice,
                router = router
            ).run(this)

            router.close()
            worker.join()

            freezeDevice.verify()

            val actualResults = resultsChannel.receiveAvailable()

            assertWithMessage("Received no messages")
                .that(actualResults)
                .hasSize(1)

            assertThat(actualResults[0])
                .isInstanceOf<DeviceWorkerMessage.WorkerDied>()
        }

    @Test
    fun `returns failed message - device is freeze while processing first intention`() =
        runBlockingTest {
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
                coordinate = randomDeviceCoordinate(),
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
            val router = IntentionsRouter(logger = logger).apply {
                intentions.forEach { sendIntention(it) }
            }

            val worker = provideDeviceWorker(
                results = resultsChannel,
                device = freezeDevice,
                router = router
            ).run(this)

            router.close()
            worker.join()

            val results = resultsChannel.receiveAvailable()

            freezeDevice.verify()

            assertWithMessage("Received only one message from worker")
                .that(results)
                .hasSize(2)

            assertWithMessage("Received only Worker failed message for freeze device")
                .that(results[0])
                .isInstanceOf<DeviceWorkerMessage.WorkerDied>()

            assertWithMessage("Received only Worker failed message for freeze device")
                .that(results[1])
                .isInstanceOf<DeviceWorkerMessage.FailedIntentionProcessing>()
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
        listener = NoOpListener,
        dispatchers = TestDispatcher,
        logger = logger
    )
}
