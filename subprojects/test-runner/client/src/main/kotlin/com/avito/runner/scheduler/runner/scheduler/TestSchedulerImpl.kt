package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.Result
import com.avito.android.TestInApk
import com.avito.android.TestSuiteLoader
import com.avito.android.check.AllChecks
import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.devices.model.ReservationData
import com.avito.android.runner.report.Report
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.config.QuotaConfigurationData
import com.avito.runner.config.Reservation
import com.avito.runner.scheduler.TestRunnerFactory
import com.avito.runner.scheduler.runner.model.ExecutionParameters
import com.avito.runner.scheduler.runner.model.TargetGroup
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.TestSchedulerResult
import com.avito.runner.scheduler.runner.model.TestWithTarget
import com.avito.runner.scheduler.suite.TestSuite
import com.avito.runner.scheduler.suite.TestSuiteProvider
import com.avito.runner.scheduler.suite.filter.FilterInfoWriter
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

internal class TestSchedulerImpl(
    private val params: InstrumentationTestsActionParams,
    private val report: Report,
    private val testSuiteProvider: TestSuiteProvider,
    private val testSuiteLoader: TestSuiteLoader,
    private val filterInfoWriter: FilterInfoWriter,
    loggerFactory: LoggerFactory,
    private val executionParameters: ExecutionParameters,
    private val outputDir: File,
    private val devicesProvider: DevicesProvider,
    private val testRunnerFactoryFactory: (List<TestWithTarget>) -> TestRunnerFactory,
) : TestScheduler {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val logger = loggerFactory.create<TestSchedulerImpl>()

    private val scope = CoroutineScope(CoroutineName("test-scheduler") + Dispatchers.IO)

    private val outputDirectoryName = "test-runner"

    private val configurationName: String = params.instrumentationConfiguration.name

    override fun schedule(): TestSchedulerResult {
        logger.debug("Filter config: ${params.instrumentationConfiguration.filter}")
        filterInfoWriter.writeFilterConfig(params.instrumentationConfiguration.filter)

        val tests = testSuiteLoader.loadTestSuite(params.testApk, AllChecks())

        tests.fold(
            { result ->
                logger.info("Tests parsed from apk: ${result.size}")
                logger.debug("Tests parsed from apk: ${result.map { it.testName }}")
            },
            { error -> logger.critical("Can't parse tests from apk", error) }
        )

        writeParsedTests(tests)

        val testSuite = testSuiteProvider.getTestSuite(
            tests = tests.getOrThrow()
        )

        val skippedTests = testSuite.skippedTests.map {
            "${it.first.test.name} on ${it.first.target.deviceName} because ${it.second.reason}"
        }
        logger.debug("Skipped tests: $skippedTests")

        val testsToRun = testSuite.testsToRun
        logger.debug("Tests to run: ${testsToRun.map { "${it.test.name} on ${it.target.deviceName}" }}")

        filterInfoWriter.writeAppliedFilter(testSuite.appliedFilter)
        filterInfoWriter.writeFilterExcludes(testSuite.skippedTests)

        writeTestSuite(testSuite)

        if (testsToRun.isNotEmpty()) {
            withDevices(reservations = reservations(testsToRun)) { devices ->

                val testRequests = testsToRun.map { targetTestRun: TestWithTarget ->

                    val reservation = targetTestRun.target.reservation

                    createTestRunRequest(
                        targetTestRun = targetTestRun,
                        quota = reservation.quota,
                        reservation = reservation,
                        application = params.mainApk,
                        testApplication = params.testApk,
                        executionParameters = executionParameters
                    )
                }

                runBlocking {
                    withContext(scope.coroutineContext) {
                        testRunnerFactoryFactory.invoke(testsToRun).createTestRunner(
                            outputDirectory = outputFolder(outputDir),
                            devices = devices
                        ).runTests(testRequests)
                    }
                }
            }

            logger.debug("Worker completed")
        }

        return TestSchedulerResult(
            testSuite = testSuite,
            testResults = report.getTestResults()
        )
    }

    private fun outputFolder(output: File): File = File(
        output,
        outputDirectoryName
    ).apply { mkdirs() }

    private fun reservations(
        tests: List<TestWithTarget>
    ): Collection<ReservationData> {

        val testsGroupedByTargets: Map<TargetGroup, List<TestWithTarget>> = tests.groupBy {
            TargetGroup(
                name = it.target.name,
                reservation = it.target.reservation
            )
        }

        return testsGroupedByTargets
            .map { (target, tests) ->
                val reservation = target.reservation.data(
                    tests = tests.map { it.test.name }
                )

                logger.info(
                    "Devices: ${reservation.count} devices will be allocated for " +
                        "target: ${target.name} inside configuration: $configurationName"
                )

                reservation
            }
    }

    // TODO: extract and delegate this channels orchestration.
    //  It's overcomplicated for local client
    private fun withDevices(
        reservations: Collection<ReservationData>,
        action: (devices: ReceiveChannel<Device>) -> Unit
    ) {
        runBlocking {
            scope.launch {
                try {
                    logger.info("Devices: Starting action job for configuration: $configurationName...")
                    action(devicesProvider.provideFor(reservations, this))
                    logger.info("Devices: Action completed for configuration: $configurationName")
                } catch (e: Throwable) {
                    logger.critical("Error during action in $configurationName job", e)
                } finally {
                    withContext(NonCancellable) {
                        logger.info("Devices: Starting releasing devices for configuration: $configurationName...")
                        devicesProvider.releaseDevices()
                        logger.info("Devices: Devices released for configuration: $configurationName")
                    }
                }
            }.join()
        }
    }

    private fun createTestRunRequest(
        targetTestRun: TestWithTarget,
        quota: QuotaConfigurationData,
        reservation: Reservation,
        application: File?,
        testApplication: File,
        executionParameters: ExecutionParameters
    ): TestRunRequest = TestRunRequest(
        testCase = TestCase(
            className = targetTestRun.test.name.className,
            methodName = targetTestRun.test.name.methodName,
            deviceName = targetTestRun.target.deviceName
        ),
        configuration = DeviceConfiguration(
            api = reservation.device.api,
            model = reservation.device.model
        ),
        scheduling = TestRunRequest.Scheduling(
            retryCount = quota.retryCount,
            minimumFailedCount = quota.minimumFailedCount,
            minimumSuccessCount = quota.minimumSuccessCount
        ),
        application = application?.absolutePath,
        applicationPackage = executionParameters.applicationPackageName,
        testApplication = testApplication.absolutePath,
        testPackage = executionParameters.applicationTestPackageName,
        testRunner = executionParameters.testRunner,
        timeoutMinutes = TEST_TIMEOUT_MINUTES,
        instrumentationParameters = targetTestRun.target.instrumentationParams,
        enableDeviceDebug = executionParameters.enableDeviceDebug
    )

    private fun writeParsedTests(parsedTests: Result<List<TestInApk>>) {
        val file = File(params.outputDir, "parsed-tests.json")
        parsedTests.fold(
            { tests -> file.writeText(gson.toJson(tests)) },
            { t -> file.writeText("There was an error while parsing tests:\n $t") }
        )
    }

    private fun writeTestSuite(testSuite: TestSuite) {
        File(params.outputDir, "test-suite.json")
            .writeText(gson.toJson(testSuite.testsToRun.map { it.test }))
    }
}

private const val TEST_TIMEOUT_MINUTES = 5L
