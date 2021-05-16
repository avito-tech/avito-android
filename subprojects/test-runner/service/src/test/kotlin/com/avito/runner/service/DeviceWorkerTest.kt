package com.avito.runner.service

import com.avito.android.Result
import com.avito.logger.StubLoggerFactory
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.State
import com.avito.runner.service.worker.DeviceWorker
import com.avito.runner.service.worker.DeviceWorkerMessage
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.Device.DeviceStatus
import com.avito.runner.service.worker.listener.CompositeDeviceListener
import com.avito.runner.service.worker.listener.DeviceListener
import com.avito.runner.service.worker.listener.FakeMethodsInvocationDeviceListener
import com.avito.runner.service.worker.listener.MessagesDeviceListener
import com.avito.runner.test.NoOpTestListener
import com.avito.runner.test.StubActionResult
import com.avito.runner.test.StubDevice
import com.avito.runner.test.StubDevice.Companion.installApplicationSuccess
import com.avito.runner.test.TestDispatcher
import com.avito.runner.test.generateInstalledApplicationLayer
import com.avito.runner.test.generateInstrumentationTestAction
import com.avito.runner.test.generateIntention
import com.avito.runner.test.listWithDefault
import com.avito.runner.test.randomDeviceCoordinate
import com.avito.runner.test.receiveAvailable
import com.avito.time.StubTimeProvider
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.ExperimentalPathApi

@ExperimentalCoroutinesApi
@ExperimentalPathApi
class DeviceWorkerTest {

    private val loggerFactory = StubLoggerFactory

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
            val successfulDevice = StubDevice(
                loggerFactory = loggerFactory,
                coordinate = randomDeviceCoordinate(),
                apiResult = StubActionResult.Success(22),
                installApplicationResults = mutableListOf(
                    installApplicationSuccess(), // Install application
                    installApplicationSuccess() // Install test application
                ),
                clearPackageResults = (0 until intentions.size - 1).flatMap {
                    listOf(
                        StubActionResult.Success(Result.Success(Unit)),
                        StubActionResult.Success(Result.Success(Unit))
                    )
                },
                gettingDeviceStatusResults = listWithDefault(
                    1 + intentions.size,
                    DeviceStatus.Alive
                ),
                runTestsResults = intentions.map {
                    StubActionResult.Success(
                        TestCaseRun.Result.Passed
                    )
                }
            )

            val resultsChannel: Channel<DeviceWorkerMessage> =
                Channel(Channel.UNLIMITED)

            val router = IntentionsRouter(loggerFactory = loggerFactory).apply {
                intentions.forEach { sendIntention(it) }
            }

            val worker = provideDeviceWorker(
                device = successfulDevice,
                router = router,
                deviceListener = MessagesDeviceListener(resultsChannel)
            ).run(this)

            router.cancel()
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
    fun `onTestFinished should be called worker run`() =
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

            val successfulDevice = StubDevice(
                loggerFactory = loggerFactory,
                coordinate = randomDeviceCoordinate(),
                apiResult = StubActionResult.Success(22),
                installApplicationResults = mutableListOf(
                    installApplicationSuccess(), // Install application
                    installApplicationSuccess() // Install test application
                ),
                clearPackageResults = (0 until intentions.size - 1).flatMap {
                    listOf(
                        StubActionResult.Success(Result.Success(Unit)),
                        StubActionResult.Success(Result.Success(Unit))
                    )
                },
                gettingDeviceStatusResults = listWithDefault(
                    1 + intentions.size,
                    DeviceStatus.Alive
                ),
                runTestsResults = intentions.map {
                    StubActionResult.Success(
                        TestCaseRun.Result.Passed
                    )
                }
            )

            val resultsChannel: Channel<DeviceWorkerMessage> =
                Channel(Channel.UNLIMITED)

            val router = IntentionsRouter(loggerFactory = loggerFactory).apply {
                intentions.forEach { sendIntention(it) }
            }

            val fakeListener = FakeMethodsInvocationDeviceListener()

            val worker = provideDeviceWorker(
                device = successfulDevice,
                router = router,
                deviceListener = CompositeDeviceListener(
                    listOf(
                        MessagesDeviceListener(resultsChannel),
                        fakeListener
                    )
                )
            ).run(this)

            router.cancel()
            worker.join()

            assertThat(fakeListener.isFinished).isTrue()
        }

    @Test
    fun `fail with device died event - device is freeze before processing intentions`() =
        runBlockingTest {
            val freezeDevice = StubDevice(
                loggerFactory = loggerFactory,
                coordinate = randomDeviceCoordinate(),
                apiResult = StubActionResult.Success(22),
                installApplicationResults = emptyList(),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Freeze(RuntimeException())
                ),
                runTestsResults = emptyList()
            )

            val resultsChannel = Channel<DeviceWorkerMessage>(Channel.UNLIMITED)
            val router = IntentionsRouter(loggerFactory = loggerFactory)

            val worker = provideDeviceWorker(
                device = freezeDevice,
                router = router,
                deviceListener = MessagesDeviceListener(resultsChannel)
            ).run(this)

            router.cancel()
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
            val freezeDevice = StubDevice(
                loggerFactory = loggerFactory,
                coordinate = randomDeviceCoordinate(),
                apiResult = StubActionResult.Success(22),
                installApplicationResults = emptyList(),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive,
                    DeviceStatus.Freeze(RuntimeException())
                ),
                runTestsResults = emptyList()
            )

            val resultsChannel: Channel<DeviceWorkerMessage> =
                Channel(Channel.UNLIMITED)
            val router = IntentionsRouter(loggerFactory = loggerFactory).apply {
                intentions.forEach { sendIntention(it) }
            }

            val worker = provideDeviceWorker(
                device = freezeDevice,
                router = router,
                deviceListener = MessagesDeviceListener(resultsChannel)
            ).run(this)

            router.cancel()
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
        device: Device,
        router: IntentionsRouter,
        deviceListener: DeviceListener
    ) = DeviceWorker(
        intentionsRouter = router,
        device = device,
        outputDirectory = File(""),
        testListener = NoOpTestListener,
        deviceListener = deviceListener,
        timeProvider = StubTimeProvider(),
        dispatchers = TestDispatcher
    )
}
