package com.avito.runner.scheduler.runner

import com.avito.android.Result
import com.avito.logger.StubLoggerFactory
import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.metrics.StubTestMetricsListener
import com.avito.runner.scheduler.report.CompositeReporter
import com.avito.runner.scheduler.report.SummaryReportMakerImpl
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.createStubInstance
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.DeviceWorkerPoolImpl
import com.avito.runner.service.listener.NoOpTestListener
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.createStubInstance
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.Device.DeviceStatus
import com.avito.runner.service.worker.device.Device.Signal
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.createStubInstance
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.runner.service.worker.device.model.createStubInstance
import com.avito.runner.service.worker.device.model.getData
import com.avito.runner.service.worker.device.stub.StubActionResult
import com.avito.runner.service.worker.device.stub.StubDevice
import com.avito.runner.service.worker.device.stub.StubDevice.Companion.installApplicationFailure
import com.avito.runner.service.worker.device.stub.StubDevice.Companion.installApplicationSuccess
import com.avito.runner.service.worker.listener.StubDeviceListener
import com.avito.test.TestDispatcher
import com.avito.time.StubTimeProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
internal class RunnerIntegrationTest {

    private val devices = Channel<Device>(Channel.UNLIMITED)

    private val state = TestRunnerExecutionState()

    private val loggerFactory = StubLoggerFactory

    @Test
    fun `all tests passed - for 1 successful device`() =
        runBlockingTest {
            val runner = provideRunner(
                devices = devices
            )

            val requests = createRunRequests()

            val device = createSuccessfulDevice(requests)

            devices.send(device)

            val result = runner.runTests(tests = requests)
            device.verify()

            assertThat(result.runs).isEqualTo(
                requests.toPassedRuns(device)
            )
        }

    @Suppress("MaxLineLength")
    @Test
    fun `all tests passed by first and second devices - first device completes half of tests and fails, second connects later and completes all remaining tests`() =
        runBlockingTest {
            val runner = provideRunner(
                devices = devices
            )

            val requests = createRunRequests()

            val firstFailedDevice = StubDevice(
                loggerFactory = loggerFactory,
                coordinate = DeviceCoordinate.Local.createStubInstance(),
                installApplicationResults = mutableListOf(
                    installApplicationSuccess(), // Install application
                    installApplicationSuccess() // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive, // Device status for initializing
                    DeviceStatus.Alive, // Device status for first test
                    DeviceStatus.Alive, // Device status for second test
                    deviceIsFreezed() // Device status for third test
                ),
                // First request doesn't need clearing package
                // Third test has freeze status
                // Last request executing on another device
                clearPackageResults = (0 until requests.size - 3).flatMap {
                    listOf(
                        succeedClearPackage(),
                        succeedClearPackage()
                    )
                },
                runTestsResults = listOf(
                    testPassed(), // Test result for first test
                    testPassed() // Test result for second test
                )
            )
            val secondDevice = StubDevice(
                loggerFactory = loggerFactory,
                coordinate = DeviceCoordinate.Local.createStubInstance(),
                installApplicationResults = mutableListOf(
                    installApplicationSuccess(), // Install application
                    installApplicationSuccess() // Install test application
                ),
                clearPackageResults = listOf(
                    succeedClearPackage(),
                    succeedClearPackage()
                ),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive, // Device status for initializing
                    DeviceStatus.Alive, // Device status for third test
                    DeviceStatus.Alive // Device status for fourth test
                ),
                runTestsResults = listOf(
                    testPassed(), // Test result for third test
                    testPassed() // Test result for fourth test
                )
            )

            launch {
                devices.send(firstFailedDevice)
                // Wait for completion 2 tests by first device
                while (!firstFailedDevice.isDone()) {
                    delay(TimeUnit.MILLISECONDS.toMillis(10))
                }
                devices.send(secondDevice)
            }

            val result = runner.runTests(requests)
            val resultsByFirstDevice = requests.slice(0..1).toPassedRuns(firstFailedDevice)

            val resultsBySecondDevice = requests.slice(2..3).toPassedRuns(secondDevice)

            firstFailedDevice.verify()
            secondDevice.verify()

            assertThat(result.runs).isEqualTo(
                resultsByFirstDevice + resultsBySecondDevice
            )
        }

    @Test
    fun `all tests passed by first device - for 1 successful and 1 freeze device`() =
        runBlockingTest {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val requests = createRunRequests()

            val successfulDevice = createSuccessfulDevice(requests)
            val failedDevice = createBrokenDevice(Exception())

            launch {
                devices.send(failedDevice)
                // Wait for completion 2 tests by first device
                while (!failedDevice.isDone()) {
                    delay(TimeUnit.MILLISECONDS.toMillis(10))
                }
                devices.send(successfulDevice)
            }

            val result = runner.runTests(requests)
            successfulDevice.verify()
            failedDevice.verify()

            assertThat(result.runs).isEqualTo(
                requests.toPassedRuns(successfulDevice)
            )
        }

    @Test
    fun `all tests passed by first device - for 1 successful and 1 failed to get status device`() =
        runBlockingTest {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val requests = createRunRequests()

            val successfulDevice = createSuccessfulDevice(requests)
            val failedDevice = createBrokenDevice(Exception())

            launch {
                devices.send(failedDevice)
                while (!failedDevice.isDone()) {
                    delay(TimeUnit.MILLISECONDS.toMillis(10))
                }
                devices.send(successfulDevice)
            }

            val result = runner.runTests(requests)
            successfulDevice.verify()
            failedDevice.verify()

            assertThat(result.runs).isEqualTo(
                requests.toPassedRuns(successfulDevice)
            )
        }

    @Test
    fun `all tests passed by first device - when second device failed on application installing`() =
        runBlockingTest {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val requests = createRunRequests()

            val successfulDevice = createSuccessfulDevice(requests)
            val failedDevice = StubDevice(
                tag = "StubDevice:installProblems",
                loggerFactory = loggerFactory,
                coordinate = DeviceCoordinate.Local.createStubInstance(),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive,
                    DeviceStatus.Alive
                ),
                installApplicationResults = mutableListOf(
                    installApplicationFailure(), // Install test application
                    installApplicationFailure() // Install application
                )
            )

            launch {
                devices.send(failedDevice)
                while (!failedDevice.isDone()) {
                    delay(TimeUnit.MILLISECONDS.toMillis(10))
                }
                devices.send(successfulDevice)
            }

            val result = runner.runTests(requests)
            successfulDevice.verify()
            failedDevice.verify()

            assertThat(result.runs).isEqualTo(
                requests.toPassedRuns(successfulDevice)
            )
        }

    @Test
    fun `test passed after retry of failed test`() = runBlockingTest {
        val devices = Channel<Device>(Channel.UNLIMITED)
        val runner = provideRunner(
            devices = devices
        )

        val scheduling = TestRunRequest.Scheduling(
            retryCount = 1,
            minimumSuccessCount = 1,
            minimumFailedCount = 0
        )

        val requests = createRunRequests(count = 2, scheduling = scheduling)

        val device = StubDevice(
            loggerFactory = loggerFactory,
            coordinate = DeviceCoordinate.Local.createStubInstance(),
            installApplicationResults = listOf(
                installApplicationSuccess(), // Install application
                installApplicationSuccess() // Install test application
            ),
            clearPackageResults = listOf(
                succeedClearPackage(), // Clear test package for first try for second test
                succeedClearPackage(), // Clear application package for first try for second test

                succeedClearPackage(), // Clear test package for second try for second test
                succeedClearPackage() // Clear application package for second try for second test
            ),
            gettingDeviceStatusResults = listOf(
                DeviceStatus.Alive, // Alive status for initializing
                DeviceStatus.Alive, // Alive status for first test
                DeviceStatus.Alive, // Alive status for first try for second test
                DeviceStatus.Alive // Alive status for second test try for second test
            ),
            runTestsResults = listOf(
                testPassed(), // First test passed
                testFailed(), // Second test failed
                testPassed() // Second test passed by second try
            )
        )

        devices.send(device)

        val result = runner.runTests(requests)
        device.verify()

        assertThat(result.runs).isEqualTo(
            mapOf(
                requests[0].let { request ->
                    request to listOf(request.toPassedRun(device)) // Passed by first try
                },
                requests[1].let { request ->
                    request to listOf(request.toFailedRun(device), request.toPassedRun(device))
                }
            )
        )
    }

    @Test
    fun `test passed after retry of failed test when minimal passed count is 2 and retry quota is 4`() =
        runBlockingTest {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val scheduling = TestRunRequest.Scheduling(
                retryCount = 4,
                minimumSuccessCount = 2,
                minimumFailedCount = 0
            )

            val requests = createRunRequests(count = 2, scheduling = scheduling)

            val device = StubDevice(
                loggerFactory = loggerFactory,
                coordinate = DeviceCoordinate.Local.createStubInstance(),
                installApplicationResults = listOf(
                    installApplicationSuccess(), // Install application
                    installApplicationSuccess() // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive, // Alive status for initializing
                    DeviceStatus.Alive, // Alive status for first try for first test
                    DeviceStatus.Alive, // Alive status for second try for first test
                    DeviceStatus.Alive, // Alive status for first try for second test
                    DeviceStatus.Alive, // Alive status for second try for second test
                    DeviceStatus.Alive, // Alive status for third try for first test
                    DeviceStatus.Alive, // Alive status for third try for second test
                    DeviceStatus.Alive // Alive status for fourth try for second test
                ),
                clearPackageResults = listOf(
                    succeedClearPackage(), // Clear test package for second try for first test
                    succeedClearPackage(), // Clear application package for second try for first test
                    succeedClearPackage(), // Clear test package for first try for second test
                    succeedClearPackage(), // Clear application package for first try for second test
                    succeedClearPackage(), // Clear test package for second try for second test
                    succeedClearPackage(), // Clear application package for second try for second test
                    succeedClearPackage(), // Clear test package for first try for third test
                    succeedClearPackage(), // Clear application package for first try for third test
                    succeedClearPackage(), // Clear test package for second try for third test
                    succeedClearPackage(), // Clear application package for second try for third test
                    succeedClearPackage(), // Clear test package for second try for fourth test
                    succeedClearPackage() // Clear application package for second try for fourth test
                ),
                runTestsResults = listOf(
                    testPassed(), // First test passed by first try
                    testFailed(), // First test failed by second try
                    testPassed(), // First test passed  by third try
                    testFailed(), // Second test failed by first try
                    testPassed(), // Second test passed by second try
                    testFailed(), // Second test failed by third try
                    testPassed() // Second test passed by fourth try
                )
            )

            devices.send(device)

            val actualResult = runner.runTests(requests)
            device.verify()

            assertThat(actualResult.runs).isEqualTo(
                mapOf(
                    requests[0].let { request ->
                        request to listOf(
                            request.toPassedRun(device),
                            request.toFailedRun(device),
                            request.toPassedRun(device)
                        )
                    },
                    requests[1].let { request ->
                        request to listOf(
                            request.toFailedRun(device),
                            request.toPassedRun(device),
                            request.toFailedRun(device),
                            request.toPassedRun(device)
                        )
                    }
                )
            )
        }

    @Test
    fun `test completed after 1 success and 1 fail for that requirements with retryCount 4`() =
        runBlockingTest {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val scheduling = TestRunRequest.Scheduling(
                retryCount = 4,
                minimumSuccessCount = 1,
                minimumFailedCount = 1
            )

            val requests = createRunRequests(count = 2, scheduling = scheduling)

            val device = StubDevice(
                loggerFactory = loggerFactory,
                coordinate = DeviceCoordinate.Local.createStubInstance(),
                installApplicationResults = listOf(
                    installApplicationSuccess(), // Install application
                    installApplicationSuccess() // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    DeviceStatus.Alive, // Alive status for initializing
                    DeviceStatus.Alive, // Alive status for first try for first test
                    DeviceStatus.Alive, // Alive status for second try for first test
                    DeviceStatus.Alive, // Alive status for first try for second test
                    DeviceStatus.Alive // Alive status for second try for second test
                ),
                clearPackageResults = listOf(
                    succeedClearPackage(), // Clear test package for second try for first test
                    succeedClearPackage(), // Clear application package for second try for first test

                    succeedClearPackage(), // Clear test package for first try for second test
                    succeedClearPackage(), // Clear application package for first try for second test

                    succeedClearPackage(), // Clear test package for second try for second test
                    succeedClearPackage() // Clear application package for second try for second test
                ),
                runTestsResults = listOf(
                    testPassed(), // First test passed by first try
                    testFailed(), // First test failed by second try
                    testPassed(), // First test passed by second try
                    testFailed() // Second test failed by second try
                )
            )

            devices.send(device)

            val result = runner.runTests(requests)
            device.verify()

            assertThat(result.runs).isEqualTo(
                mapOf(
                    requests[0].let { request ->
                        request to listOf(request.toPassedRun(device), request.toFailedRun(device))
                    },
                    requests[1].let { request ->
                        request to listOf(request.toPassedRun(device), request.toFailedRun(device))
                    }
                )
            )
        }

    @Test
    fun `devices channel closed - run failed`() {
        val exception = assertThrows<IllegalStateException> {
            runBlockingTest {
                val devices = Channel<Device>(Channel.UNLIMITED)
                devices.close()
                val runner = provideRunner(
                    devices = devices
                )

                val scheduling = TestRunRequest.Scheduling(
                    retryCount = 4,
                    minimumSuccessCount = 1,
                    minimumFailedCount = 1
                )

                val requests = createRunRequests(count = 2, scheduling = scheduling)

                runner.runTests(requests)
            }
        }

        assertThat(exception.message)
            .isEqualTo("devices channel was closed")
    }

    private fun createBrokenDevice(failureReason: Exception): StubDevice {
        return StubDevice(
            tag = "StubDevice:broken",
            loggerFactory = loggerFactory,
            coordinate = DeviceCoordinate.Local.createStubInstance(),
            gettingDeviceStatusResults = listOf(
                DeviceStatus.Freeze(failureReason)
            )
        )
    }

    private fun createRunRequests(
        count: Int = 4,
        scheduling: TestRunRequest.Scheduling = TestRunRequest.Scheduling.createStubInstance()
    ): List<TestRunRequest> {
        return (1..count).map { index ->
            testRunRequest(
                test = TestCase.createStubInstance(methodName = "test_$index"),
                scheduling = scheduling
            )
        }
    }

    private fun List<TestRunRequest>.toPassedRuns(device: StubDevice): Map<TestRunRequest, List<DeviceTestCaseRun>> {
        return map { request ->
            request to listOf(request.toPassedRun(device))
        }.toMap()
    }

    private fun TestRunRequest.toPassedRun(
        device: StubDevice
    ): DeviceTestCaseRun {
        return deviceTestCaseRun(
            device = device,
            test = testCase,
            result = TestCaseRun.Result.Passed
        )
    }

    private fun TestRunRequest.toFailedRun(
        device: StubDevice
    ): DeviceTestCaseRun {
        return deviceTestCaseRun(
            device = device,
            test = testCase,
            result = TestCaseRun.Result.Failed.InRun("Failed")
        )
    }

    private fun deviceIsFreezed(): DeviceStatus {
        return DeviceStatus.Freeze(reason = Exception())
    }

    private fun testFailed(): StubActionResult.Success<TestCaseRun.Result> {
        return StubActionResult.Success(
            TestCaseRun.Result.Failed.InRun("Failed")
        )
    }

    private fun createSuccessfulDevice(requests: List<TestRunRequest>): StubDevice {
        return StubDevice(
            tag = "StubDevice:normal",
            loggerFactory = loggerFactory,
            coordinate = DeviceCoordinate.Local.createStubInstance(),
            installApplicationResults = mutableListOf(
                installApplicationSuccess(), // Install application
                installApplicationSuccess() // Install test application
            ),
            clearPackageResults = (0 until requests.size - 1).flatMap {
                listOf(
                    succeedClearPackage(),
                    succeedClearPackage()
                )
            },
            gettingDeviceStatusResults = List(requests.size + 1) { DeviceStatus.Alive },
            runTestsResults = requests.map {
                testPassed()
            }
        )
    }

    private fun testPassed(): StubActionResult.Success<TestCaseRun.Result> {
        return StubActionResult.Success(
            TestCaseRun.Result.Passed
        )
    }

    private fun succeedClearPackage() = StubActionResult.Success<Result<Unit>>(Result.Success(Unit))

    private fun provideRunner(
        devices: ReceiveChannel<Device>,
        testListener: TestListener = NoOpTestListener,
        outputDirectory: File = File("")
    ): TestRunner {
        val scheduler = TestExecutionScheduler(
            dispatcher = TestCoroutineDispatcher(),
            results = state.results,
            intentions = state.intentions,
            intentionResults = state.intentionResults
        )
        val deviceWorkerPool = DeviceWorkerPoolImpl(
            outputDirectory = outputDirectory,
            loggerFactory = loggerFactory,
            testListener = testListener,
            deviceMetricsListener = StubDeviceListener(),
            deviceWorkersDispatcher = TestDispatcher,
            timeProvider = StubTimeProvider(),
            state = DeviceWorkerPool.State(
                devices = devices,
                intentions = state.intentions,
                intentionResults = state.intentionResults,
                deviceSignals = state.deviceSignals,
            )
        )

        return TestRunnerImpl(
            scheduler = scheduler,
            deviceWorkerPool = deviceWorkerPool,
            loggerFactory = loggerFactory,
            state = state,
            reservationWatcher = object : DeviceReservationWatcher {
                override fun watch(deviceSignals: ReceiveChannel<Signal>, scope: CoroutineScope) {
                    // empty
                }
            },
            summaryReportMaker = SummaryReportMakerImpl(),
            reporter = CompositeReporter(emptyList()),
            testMetricsListener = StubTestMetricsListener
        )
    }

    private fun testRunRequest(
        test: TestCase,
        apiLevel: Int = 22,
        scheduling: TestRunRequest.Scheduling = TestRunRequest.Scheduling.createStubInstance()
    ): TestRunRequest = TestRunRequest.Companion.createStubInstance(
        scheduling = scheduling,
        testCase = test,
        deviceConfiguration = DeviceConfiguration.createStubInstance(api = apiLevel)
    )

    private fun deviceTestCaseRun(
        device: Device,
        test: TestCase,
        result: TestCaseRun.Result
    ): DeviceTestCaseRun = DeviceTestCaseRun(
        testCaseRun = TestCaseRun(
            test = test,
            result = result,
            timestampStartedMilliseconds = 0,
            timestampCompletedMilliseconds = 0
        ),
        device = device.getData()
    )
}
