package com.avito.runner.scheduler.runner

import com.avito.runner.logging.Logger
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
import com.avito.runner.service.worker.device.observer.DevicesObserver
import com.avito.runner.test.NoOpListener
import com.avito.runner.test.mock.MockActionResult
import com.avito.runner.test.mock.MockDevice
import com.avito.runner.test.mock.MockDevicesObserver
import com.avito.runner.test.randomString
import com.avito.runner.test.runBlockingWithTimeout
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.GlobalScope
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
            val observer = MockDevicesObserver()

            val runner = provideRunner(
                observer = observer
            )

            val requests = listOf(
                testRunRequest(),
                testRunRequest(),
                testRunRequest(),
                testRunRequest()
            )

            val device = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any())  // Install test application
                ),
                gettingDeviceStatusResults = requests.map {
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    )
                },
                clearPackageResults = (0 until requests.size - 1).flatMap {
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
                runTestsResults = requests.map {
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    )
                }
            )

            GlobalScope.launch {
                observer.send(device)
            }

            val result = runner.runTests(tests = requests)

            device.verify()

            assertThat(
                result.runs
            ).isEqualTo(
                requests
                    .map { request ->
                        request to listOf(
                            deviceTestCaseRun(
                                device = device,
                                test = request.testCase,
                                result = TestCaseRun.Result.Passed
                            )
                        )
                    }
                    .toMap()
            )
        }

    @Test
    fun `all tests passed by first and second devices - for first device that complete half of tests and failed and second connected later device that complete all remaining tests`() =
        runBlockingWithTimeout {
            val observer = MockDevicesObserver()

            val runner = provideRunner(
                observer = observer
            )

            val requests = listOf(
                testRunRequest(),
                testRunRequest(),
                testRunRequest(),
                testRunRequest()
            )

            val firstFailedDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any())  // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Device status for first test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Device status for second test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Freeze(reason = Exception())
                    ) // Device status for third test
                ),
                // First request doesn't need clearing package
                // Third test has freeze status
                // Last request executing on another device
                clearPackageResults = (0 until requests.size - 3).flatMap {
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
                runTestsResults = listOf(
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    ), // Test result for first test
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    )  // Test result for second test
                )
            )
            val secondDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any())  // Install test application
                ),
                clearPackageResults = listOf(
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)),
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit))
                ),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Device status for third test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ) // Device status for fourth test
                ),
                runTestsResults = listOf(
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    ), // Test result for third test
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    ) // Test result for fourth test
                )
            )

            GlobalScope.launch {
                observer.send(firstFailedDevice)
                delay(TimeUnit.SECONDS.toMillis(3)) // Wait for completion 2 tests by first device
                observer.send(secondDevice)
            }

            val result = runner.runTests(requests)

            val resultsByFirstDevice = requests
                .slice(0..1)
                .map {
                    it to listOf(
                        deviceTestCaseRun(
                            device = firstFailedDevice,
                            test = it.testCase,
                            result = TestCaseRun.Result.Passed
                        )
                    )
                }
                .toMap()

            val resultsBySecondDevice = requests
                .slice(2..3)
                .map {
                    it to listOf(
                        deviceTestCaseRun(
                            device = secondDevice,
                            test = it.testCase,
                            result = TestCaseRun.Result.Passed
                        )
                    )
                }
                .toMap()

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
            val observer = MockDevicesObserver()

            val runner = provideRunner(
                observer = observer
            )

            val requests = listOf(
                testRunRequest(),
                testRunRequest(),
                testRunRequest(),
                testRunRequest()
            )

            val successfulDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                clearPackageResults = (0 until requests.size - 1).flatMap {
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
                gettingDeviceStatusResults = requests.map {
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    )
                },
                runTestsResults = requests.map {
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    )
                }
            )
            val failedDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Freeze(
                            reason = Exception()
                        )
                    )
                )
            )

            GlobalScope.launch {
                observer.send(failedDevice)
                delay(TimeUnit.SECONDS.toMillis(3)) // Wait for first device freeze event
                observer.send(successfulDevice)
            }

            val result = runner.runTests(requests)

            successfulDevice.verify()
            failedDevice.verify()

            assertThat(
                result.runs
            ).isEqualTo(
                requests
                    .map {
                        it to listOf(
                            deviceTestCaseRun(
                                device = successfulDevice,
                                test = it.testCase,
                                result = TestCaseRun.Result.Passed
                            )
                        )
                    }
                    .toMap()
            )
        }

    @Test
    fun `all tests passed by first device - for 1 successful and 1 failed to get status device`() =
        runBlockingWithTimeout {
            val observer = MockDevicesObserver()

            val runner = provideRunner(
                observer = observer
            )

            val requests = listOf(
                testRunRequest(),
                testRunRequest(),
                testRunRequest(),
                testRunRequest()
            )

            val successfulDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                clearPackageResults = (0 until requests.size - 1).flatMap {
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
                gettingDeviceStatusResults = requests.map {
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    )
                },
                runTestsResults = requests.map {
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    )
                }
            )
            val failedDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Failed(Exception())
                )
            )

            GlobalScope.launch {
                observer.send(failedDevice)
                delay(TimeUnit.SECONDS.toMillis(3)) // Wait for first device freeze event
                observer.send(successfulDevice)
            }

            val result = runner.runTests(requests)

            successfulDevice.verify()
            failedDevice.verify()

            assertThat(
                result.runs
            ).isEqualTo(
                requests
                    .map {
                        it to listOf(
                            deviceTestCaseRun(
                                device = successfulDevice,
                                test = it.testCase,
                                result = TestCaseRun.Result.Passed
                            )
                        )
                    }
                    .toMap()
            )
        }

    @Test
    fun `all tests passed by first device - when second device failed on application installing`() =
        runBlockingWithTimeout {
            val observer = MockDevicesObserver()

            val runner = provideRunner(
                observer = observer
            )

            val requests = listOf(
                testRunRequest(),
                testRunRequest(),
                testRunRequest(),
                testRunRequest()
            )

            val successfulDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                installApplicationResults = mutableListOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                clearPackageResults = (0 until requests.size - 1).flatMap {
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
                gettingDeviceStatusResults = requests.map {
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    )
                },
                runTestsResults = requests.map {
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    )
                }
            )
            val failedDevice = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    )
                ),
                installApplicationResults = mutableListOf(
                    MockActionResult.Failed(Exception())
                )
            )

            GlobalScope.launch {
                observer.send(failedDevice)
                delay(TimeUnit.SECONDS.toMillis(3)) // Wait for first device installation fail event
                observer.send(successfulDevice)
            }

            val result = runner.runTests(requests)

            successfulDevice.verify()
            failedDevice.verify()

            assertThat(
                result.runs
            ).isEqualTo(
                requests
                    .map {
                        it to listOf(
                            deviceTestCaseRun(
                                device = successfulDevice,
                                test = it.testCase,
                                result = TestCaseRun.Result.Passed
                            )
                        )
                    }
                    .toMap()
            )
        }

    @Test
    fun `test passed after retry of failed test`() = runBlockingWithTimeout {
        val observer = MockDevicesObserver()

        val scheduling = TestRunRequest.Scheduling(
            retryCount = 1,
            minimumSuccessCount = 1,
            minimumFailedCount = 0
        )

        val runner = provideRunner(
            observer = observer
        )

        val requests = listOf(
            testRunRequest(scheduling = scheduling),
            testRunRequest(scheduling = scheduling)
        )

        val device = MockDevice(
            logger = StdOutLogger(),
            id = randomString(),
            installApplicationResults = listOf(
                MockActionResult.Success(Any()), // Install application
                MockActionResult.Success(Any()) // Install test application
            ),
            clearPackageResults = listOf(
                MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for first try for second test
                MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear application package for first try for second test

                MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for second try for second test
                MockActionResult.Success<Try<Any>>(Try.Success(Unit)) // Clear application package for second try for second test
            ),
            gettingDeviceStatusResults = listOf(
                MockActionResult.Success<Device.DeviceStatus>(
                    Device.DeviceStatus.Alive
                ), // Alive status for first test
                MockActionResult.Success<Device.DeviceStatus>(
                    Device.DeviceStatus.Alive
                ), // Alive status for first try for second test
                MockActionResult.Success<Device.DeviceStatus>(
                    Device.DeviceStatus.Alive
                ) // Alive status for second test try for second test
            ),
            runTestsResults = listOf(
                MockActionResult.Success<TestCaseRun.Result>(
                    TestCaseRun.Result.Passed
                ), // First test passed
                MockActionResult.Success<TestCaseRun.Result>(
                    TestCaseRun.Result.Failed("Failed")
                ), // Second test failed
                MockActionResult.Success<TestCaseRun.Result>(
                    TestCaseRun.Result.Passed
                ) // Second test passed by second try
            )
        )

        GlobalScope.launch {
            observer.send(device)
        }

        val result = runner.runTests(requests)

        device.verify()

        assertThat(
            result.runs
        ).isEqualTo(
            mapOf(
                requests[0] to listOf(
                    deviceTestCaseRun(
                        device = device,
                        test = requests[0].testCase,
                        result = TestCaseRun.Result.Passed
                    ) // Passed by first try
                ),
                requests[1] to listOf(
                    deviceTestCaseRun(
                        device = device,
                        test = requests[1].testCase,
                        result = TestCaseRun.Result.Failed("Failed")
                    ), // First try failed
                    deviceTestCaseRun(
                        device = device,
                        test = requests[1].testCase,
                        result = TestCaseRun.Result.Passed
                    ) // Second try passed
                )
            )
        )
    }

    @Test
    fun `test passed after retry of failed test when minimal passed count is 2 and retry quota is 4`() =
        runBlockingWithTimeout {
            val observer = MockDevicesObserver()

            val scheduling = TestRunRequest.Scheduling(
                retryCount = 4,
                minimumSuccessCount = 2,
                minimumFailedCount = 0
            )

            val runner = provideRunner(
                observer = observer
            )

            val requests = listOf(
                testRunRequest(scheduling = scheduling),
                testRunRequest(scheduling = scheduling)
            )

            val device = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                installApplicationResults = listOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Alive status for first try for first test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Alive status for second try for first test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Alive status for first try for second test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Alive status for second try for second test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Alive status for third try for first test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Alive status for third try for second test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    )  // Alive status for fourth try for second test
                ),
                clearPackageResults = listOf(
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for second try for first test
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear application package for second try for first test

                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for first try for second test
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear application package for first try for second test

                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for second try for second test
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear application package for second try for second test

                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for first try for third test
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear application package for first try for third test

                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for second try for third test
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear application package for second try for third test

                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for second try for fourth test
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)) // Clear application package for second try for fourth test
                ),
                runTestsResults = listOf(
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    ), // First test passed by first try
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Failed("Failed")
                    ), // First test failed by second try
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    ), // Second test passed by first try
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Failed("Failed")
                    ), // Second test failed by second try
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    ), // First test passed  by third try
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Failed("Failed")
                    ), // Second test failed by third try
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    ) // Second test passed by fourth try
                )
            )

            GlobalScope.launch {
                observer.send(device)
            }

            val result = runner.runTests(requests)

            device.verify()

            assertThat(
                result.runs
            ).isEqualTo(
                mapOf(
                    requests[0] to listOf( //passed by first try
                        deviceTestCaseRun(
                            device = device,
                            test = requests[0].testCase,
                            result = TestCaseRun.Result.Passed
                        ),
                        deviceTestCaseRun(
                            device = device,
                            test = requests[0].testCase,
                            result = TestCaseRun.Result.Failed("Failed")
                        ),
                        deviceTestCaseRun(
                            device = device,
                            test = requests[0].testCase,
                            result = TestCaseRun.Result.Passed
                        )
                    ),
                    requests[1] to listOf(
                        deviceTestCaseRun(
                            device = device,
                            test = requests[1].testCase,
                            result = TestCaseRun.Result.Passed
                        ),
                        deviceTestCaseRun(
                            device = device,
                            test = requests[1].testCase,
                            result = TestCaseRun.Result.Failed("Failed")
                        ),
                        deviceTestCaseRun(
                            device = device,
                            test = requests[1].testCase,
                            result = TestCaseRun.Result.Failed("Failed")
                        ),
                        deviceTestCaseRun(
                            device = device,
                            test = requests[1].testCase,
                            result = TestCaseRun.Result.Passed
                        )
                    )
                )
            )
        }

    @Test
    fun `test completed after 1 success and 1 fail for that requirements with retryCount 4`() =
        runBlockingWithTimeout {
            val observer = MockDevicesObserver()

            val scheduling = TestRunRequest.Scheduling(
                retryCount = 4,
                minimumSuccessCount = 1,
                minimumFailedCount = 1
            )

            val runner = provideRunner(
                observer = observer
            )

            val requests = listOf(
                testRunRequest(scheduling = scheduling),
                testRunRequest(scheduling = scheduling)
            )

            val device = MockDevice(
                logger = StdOutLogger(),
                id = randomString(),
                installApplicationResults = listOf(
                    MockActionResult.Success(Any()), // Install application
                    MockActionResult.Success(Any()) // Install test application
                ),
                gettingDeviceStatusResults = listOf(
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Alive status for first try for first test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Alive status for second try for first test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ), // Alive status for first try for second test
                    MockActionResult.Success<Device.DeviceStatus>(
                        Device.DeviceStatus.Alive
                    ) // Alive status for second try for second test
                ),
                clearPackageResults = listOf(
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for second try for first test
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear application package for second try for first test

                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for first try for second test
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear application package for first try for second test

                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)), // Clear test package for second try for second test
                    MockActionResult.Success<Try<Any>>(Try.Success(Unit)) // Clear application package for second try for second test
                ),
                runTestsResults = listOf(
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    ), // First test passed by first try
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Failed("Failed")
                    ), // First test failed by second try
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Passed
                    ), // First test passed by second try
                    MockActionResult.Success<TestCaseRun.Result>(
                        TestCaseRun.Result.Failed("Failed")
                    ) // Second test failed by second try
                )
            )

            GlobalScope.launch {
                observer.send(device)
            }

            val result = runner.runTests(requests)

            device.verify()

            assertThat(
                result.runs
            ).isEqualTo(
                mapOf(
                    requests[0] to listOf(
                        deviceTestCaseRun(
                            device = device,
                            test = requests[0].testCase,
                            result = TestCaseRun.Result.Passed
                        ),
                        deviceTestCaseRun(
                            device = device,
                            test = requests[0].testCase,
                            result = TestCaseRun.Result.Failed("Failed")
                        )
                    ),
                    requests[1] to listOf(
                        deviceTestCaseRun(
                            device = device,
                            test = requests[1].testCase,
                            result = TestCaseRun.Result.Passed
                        ),
                        deviceTestCaseRun(
                            device = device,
                            test = requests[1].testCase,
                            result = TestCaseRun.Result.Failed("Failed")
                        )
                    )
                )
            )
        }

    private fun provideRunner(
        observer: DevicesObserver,
        testListener: TestListener = NoOpListener,
        logger: Logger = StdOutLogger(),
        outputDirectory: File = File("")
    ): TestRunner {
        val scheduler = TestExecutionScheduler(
            logger = logger
        )
        val client = TestExecutionClient()
        val service = IntentionExecutionServiceImplementation(
            outputDirectory = outputDirectory,
            logger = logger,
            devicesObserver = observer,
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
            api = apiLevel
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
