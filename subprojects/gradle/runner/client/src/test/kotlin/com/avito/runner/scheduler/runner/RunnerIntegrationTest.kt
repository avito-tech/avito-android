package com.avito.runner.scheduler.runner

import com.avito.logger.Logger
import com.avito.logger.NoOpLogger
import com.avito.runner.logging.StdOutLogger
import com.avito.runner.scheduler.runner.client.TestExecutionClient
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.scheduler.util.generateTestRunRequest
import com.avito.runner.service.IntentionExecutionServiceImplementation
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.runner.service.worker.device.model.getData
import com.avito.runner.test.NoOpListener
import com.avito.runner.test.listWithDefault
import com.avito.runner.test.mock.MockActionResult
import com.avito.runner.test.mock.MockDevice
import com.avito.runner.test.randomSerial
import com.avito.runner.test.randomString
import com.avito.runner.test.runBlockingWithTimeout
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.TimeUnit

class RunnerIntegrationTest {

    @Test
    fun `all tests passed - for 1 successful device`() =
        runBlockingWithTimeout {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val requests = createRunRequests()

            val device = createSuccessfulDevice(requests)

            GlobalScope.launch {
                devices.send(device)
            }

            val result = runner.runTests(tests = requests)

            device.verify()

            assertThat(result.runs).isEqualTo(
                requests.toPassedRuns(device)
            )
        }

    @Test
    fun `all tests passed by first and second devices - for first device that complete half of tests and failed and second connected later device that complete all remaining tests`() =
        runBlockingWithTimeout {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val requests = createRunRequests()

            val firstFailedDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomSerial(),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any())  // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    deviceIsAlive(), // Device status for initializing
                    deviceIsAlive(), // Device status for first test
                    deviceIsAlive(), // Device status for second test
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
                    testPassed()  // Test result for second test
                )
            )
            val secondDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomSerial(),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any())  // Install test application
                ),
                clearPackageResults = listOf(
                    succeedClearPackage(),
                    succeedClearPackage()
                ),
                gettingDeviceStatusResults = listOf(
                    deviceIsAlive(), // Device status for initializing
                    deviceIsAlive(), // Device status for third test
                    deviceIsAlive() // Device status for fourth test
                ),
                runTestsResults = listOf(
                    testPassed(), // Test result for third test
                    testPassed() // Test result for fourth test
                )
            )

            GlobalScope.launch {
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

            assertThat(
                result.runs
            ).isEqualTo(
                resultsByFirstDevice + resultsBySecondDevice
            )
        }

    @Test
    fun `all tests passed by first device - for 1 successful and 1 freeze device`() =
        runBlockingWithTimeout {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val requests = createRunRequests()

            val successfulDevice = createSuccessfulDevice(requests)
            val failedDevice = createBrokenDevice(deviceIsFreezed())

            GlobalScope.launch {
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

            assertThat(
                result.runs
            ).isEqualTo(
                requests.toPassedRuns(successfulDevice)
            )
        }

    @Test
    fun `all tests passed by first device - for 1 successful and 1 failed to get status device`() =
        runBlockingWithTimeout {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val requests = createRunRequests()

            val successfulDevice = createSuccessfulDevice(requests)
            val failedDevice = createBrokenDevice(MockActionResult.Failed(Exception()))

            GlobalScope.launch {
                devices.send(failedDevice)
                while (!failedDevice.isDone()) {
                    delay(TimeUnit.MILLISECONDS.toMillis(10))
                }
                devices.send(successfulDevice)
            }

            val result = runner.runTests(requests)

            successfulDevice.verify()
            failedDevice.verify()

            assertThat(
                result.runs
            ).isEqualTo(
                requests.toPassedRuns(successfulDevice)
            )
        }

    @Test
    fun `all tests passed by first device - when second device failed on application installing`() =
        runBlockingWithTimeout {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val requests = createRunRequests()

            val successfulDevice = createSuccessfulDevice(requests)
            val failedDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomSerial(),
                gettingDeviceStatusResults = listOf(
                    deviceIsAlive(), // Device state for initializing
                    deviceIsAlive()
                ),
                installApplicationResults = mutableListOf(
                    MockActionResult.Failed(Exception())
                )
            )

            GlobalScope.launch {
                devices.send(failedDevice)
                while (!failedDevice.isDone()) {
                    delay(TimeUnit.MILLISECONDS.toMillis(10))
                }
                devices.send(successfulDevice)
            }

            val result = runner.runTests(requests)

            successfulDevice.verify()
            failedDevice.verify()

            assertThat(
                result.runs
            ).isEqualTo(
                requests.toPassedRuns(successfulDevice)
            )
        }

    @Test
    fun `test passed after retry of failed test`() = runBlockingWithTimeout {
        val devices = Channel<Device>(Channel.UNLIMITED)
        val runner = provideRunner(
            devices = devices
        )

        val scheduling = TestRunRequest.Scheduling(
            retryCount = 1,
            minimumSuccessCount = 1,
            minimumFailedCount = 0
        )


        val requests = listOf(
            testRunRequest(scheduling = scheduling),
            testRunRequest(scheduling = scheduling)
        )

        val device = MockDevice(
            logger = StdOutLogger(),
            id = randomSerial(),
            installApplicationResults = listOf(
                MockActionResult.Success(Any()), // Install application
                MockActionResult.Success(Any()) // Install test application
            ),
            clearPackageResults = listOf(
                succeedClearPackage(), // Clear test package for first try for second test
                succeedClearPackage(), // Clear application package for first try for second test

                succeedClearPackage(), // Clear test package for second try for second test
                succeedClearPackage() // Clear application package for second try for second test
            ),
            gettingDeviceStatusResults = listOf(
                deviceIsAlive(), // Alive status for initializing
                deviceIsAlive(), // Alive status for first test
                deviceIsAlive(), // Alive status for first try for second test
                deviceIsAlive() // Alive status for second test try for second test
            ),
            runTestsResults = listOf(
                testPassed(), // First test passed
                testFailed(), // Second test failed
                testPassed() // Second test passed by second try
            )
        )

        GlobalScope.launch {
            devices.send(device)
        }

        val result = runner.runTests(requests)

        device.verify()

        assertThat(
            result.runs
        ).isEqualTo(
            mapOf(
                requests[0].let { request ->
                    request to listOf(request.toPassedRun(device))  // Passed by first try
                },
                requests[1].let { request ->
                    request to listOf(request.toFailedRun(device), request.toPassedRun(device))
                }
            )
        )
    }

    @Test
    fun `test passed after retry of failed test when minimal passed count is 2 and retry quota is 4`() =
        runBlockingWithTimeout {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val scheduling = TestRunRequest.Scheduling(
                retryCount = 4,
                minimumSuccessCount = 2,
                minimumFailedCount = 0
            )

            val requests = listOf(
                testRunRequest(scheduling = scheduling),
                testRunRequest(scheduling = scheduling)
            )

            val device = MockDevice(
                logger = StdOutLogger(),
                id = randomSerial(),
                installApplicationResults = listOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    deviceIsAlive(), // Alive status for initializing
                    deviceIsAlive(), // Alive status for first try for first test
                    deviceIsAlive(), // Alive status for second try for first test
                    deviceIsAlive(), // Alive status for first try for second test
                    deviceIsAlive(), // Alive status for second try for second test
                    deviceIsAlive(), // Alive status for third try for first test
                    deviceIsAlive(), // Alive status for third try for second test
                    deviceIsAlive()  // Alive status for fourth try for second test
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
                    testPassed(), // Second test passed by first try
                    testFailed(), // Second test failed by second try
                    testPassed(), // First test passed  by third try
                    testFailed(), // Second test failed by third try
                    testPassed() // Second test passed by fourth try
                )
            )

            GlobalScope.launch {
                devices.send(device)
            }

            val result = runner.runTests(requests)

            device.verify()

            assertThat(
                result.runs
            ).isEqualTo(
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
                            request.toPassedRun(device),
                            request.toFailedRun(device),
                            request.toFailedRun(device),
                            request.toPassedRun(device)
                        )
                    }
                )
            )
        }

    @Test
    fun `test completed after 1 success and 1 fail for that requirements with retryCount 4`() =
        runBlockingWithTimeout {
            val devices = Channel<Device>(Channel.UNLIMITED)
            val runner = provideRunner(
                devices = devices
            )

            val scheduling = TestRunRequest.Scheduling(
                retryCount = 4,
                minimumSuccessCount = 1,
                minimumFailedCount = 1
            )

            val requests = listOf(
                testRunRequest(scheduling = scheduling),
                testRunRequest(scheduling = scheduling)
            )

            val device = MockDevice(
                logger = StdOutLogger(),
                id = randomSerial(),
                installApplicationResults = listOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    deviceIsAlive(), // Alive status for initializing
                    deviceIsAlive(), // Alive status for first try for first test
                    deviceIsAlive(), // Alive status for second try for first test
                    deviceIsAlive(), // Alive status for first try for second test
                    deviceIsAlive() // Alive status for second try for second test
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

            GlobalScope.launch {
                devices.send(device)
            }

            val result = runner.runTests(requests)

            device.verify()

            assertThat(
                result.runs
            ).isEqualTo(
                mapOf(
                    requests[0].let { request ->
                        request to listOf(request.toPassedRun(device), request.toFailedRun(device))
                    },
                    requests[1].let { request ->
                        request to listOf(request.toPassedRun(device), request.toFailedRun(device))
                    })
            )
        }

    private fun createBrokenDevice(
        failureReason: MockActionResult<Device.DeviceStatus>
    ): MockDevice {
        return MockDevice(
            logger = StdOutLogger(),
            id = randomSerial(),
            gettingDeviceStatusResults = listOf(
                failureReason
            )
        )
    }

    private fun createRunRequests(count: Int = 4): List<TestRunRequest> {
        return (1..count).map { testRunRequest() }
    }

    private fun List<TestRunRequest>.toPassedRuns(device: MockDevice): Map<TestRunRequest, List<DeviceTestCaseRun>> {
        return map { request ->
            request to listOf(request.toPassedRun(device))
        }.toMap()
    }

    private fun TestRunRequest.toPassedRun(
        device: MockDevice
    ): DeviceTestCaseRun {
        return deviceTestCaseRun(
            device = device,
            test = testCase,
            result = TestCaseRun.Result.Passed
        )
    }

    private fun TestRunRequest.toFailedRun(
        device: MockDevice
    ): DeviceTestCaseRun {
        return deviceTestCaseRun(
            device = device,
            test = testCase,
            result = TestCaseRun.Result.Failed.InRun("Failed")
        )
    }

    private fun deviceIsFreezed(): MockActionResult.Success<Device.DeviceStatus> {
        return MockActionResult.Success(
            Device.DeviceStatus.Freeze(reason = Exception())
        )
    }

    private fun testFailed(): MockActionResult.Success<TestCaseRun.Result> {
        return MockActionResult.Success(
            TestCaseRun.Result.Failed.InRun("Failed")
        )
    }

    private fun createSuccessfulDevice(requests: List<TestRunRequest>): MockDevice {
        return MockDevice(
            logger = StdOutLogger(),
            id = randomSerial(),
            installApplicationResults = mutableListOf(
                MockActionResult.Success(Any()), // Install application
                MockActionResult.Success(Any()) // Install test application
            ),
            clearPackageResults = (0 until requests.size - 1).flatMap {
                listOf(
                    succeedClearPackage(),
                    succeedClearPackage()
                )
            },
            gettingDeviceStatusResults = listWithDefault(
                1 + requests.size,
                deviceIsAlive()
            ),
            runTestsResults = requests.map {
                testPassed()
            }
        )
    }

    private fun testPassed(): MockActionResult.Success<TestCaseRun.Result> {
        return MockActionResult.Success(
            TestCaseRun.Result.Passed
        )
    }

    private fun succeedClearPackage() = MockActionResult.Success<Try<Any>>(Try.Success(Unit))

    private fun deviceIsAlive(): MockActionResult.Success<Device.DeviceStatus> {
        return MockActionResult.Success(
            Device.DeviceStatus.Alive
        )
    }

    private fun provideRunner(
        devices: ReceiveChannel<Device>,
        testListener: TestListener = NoOpListener,
        logger: Logger = NoOpLogger,
        outputDirectory: File = File("")
    ): TestRunner {
        val scheduler = TestExecutionScheduler(
            logger = logger
        )
        val client = TestExecutionClient()
        val service = IntentionExecutionServiceImplementation(
            outputDirectory = outputDirectory,
            logger = logger,
            devices = devices,
            listener = testListener
        )

        return TestRunnerImplementation(
            scheduler = scheduler,
            client = client,
            service = service,
            logger = logger
        )
    }

    private fun testCase(): TestCase =
        TestCase(
            className = "class",
            methodName = randomString(),
            deviceName = randomString()
        )

    private fun testRunRequest(
        test: TestCase = testCase(),
        apiLevel: Int = 22,
        scheduling: TestRunRequest.Scheduling = TestRunRequest.Scheduling(
            retryCount = 0,
            minimumSuccessCount = 1,
            minimumFailedCount = 0
        )
    ): TestRunRequest = generateTestRunRequest(
        scheduling = scheduling,
        testCase = test,
        timeoutMinutes = 5,
        application = "application path",
        applicationPackage = "application package",
        testApplication = "test application path",
        testPackage = "test package",
        deviceConfiguration = DeviceConfiguration(
            api = apiLevel,
            model = "model"
        )
    )

    private fun deviceTestCaseRun(
        device: Device,
        test: TestCase,
        result: TestCaseRun.Result
    ): DeviceTestCaseRun =
        DeviceTestCaseRun(
            testCaseRun = TestCaseRun(
                test = test,
                result = result,
                timestampStartedMilliseconds = 0,
                timestampCompletedMilliseconds = 0
            ),
            device = device.getData()
        )
}
