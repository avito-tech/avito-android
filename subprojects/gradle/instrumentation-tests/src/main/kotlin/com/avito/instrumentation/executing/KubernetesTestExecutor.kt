package com.avito.instrumentation.executing

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.report.listener.TestReporter
import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.reservation.client.kubernetes.KubernetesReservationClient
import com.avito.instrumentation.reservation.client.withDevices
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.runner.logging.Logger
import com.avito.runner.scheduler.TestsRunnerClient
import com.avito.runner.scheduler.args.Arguments
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.createKubernetesClient
import com.avito.utils.logging.CILogger
import java.io.File

class KubernetesTestExecutor(
    private val logger: CILogger,
    private val kubernetesCredentials: KubernetesCredentials,
    private val buildId: String,
    private val buildType: String,
    private val projectName: String,
    private val testReporter: TestReporter?,
    private val registry: String
) : TestExecutor {

    private val runner = TestsRunnerClient()

    //todo hi Juno!
    private val outputDirectoryName = "composer"

    override fun execute(
        application: File,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        configuration: InstrumentationConfiguration.Data,
        runType: TestExecutor.RunType,
        output: File,
        logcatDir: File
    ) {
        val kubernetesReservation = KubernetesReservationClient(
            androidDebugBridge = AndroidDebugBridge(
                logger = { logger.info(it) }
            ),
            kubernetesClient = createKubernetesClient(
                kubernetesCredentials = kubernetesCredentials,
                namespace = executionParameters.namespace
            ),
            configurationName = configuration.name,
            projectName = projectName,
            logger = logger,
            buildId = buildId,
            buildType = buildType,
            emulatorsLogsReporter = EmulatorsLogsReporter(
                outputFolder = output,
                logcatTags = executionParameters.logcatTags,
                logcatDir = logcatDir
            ),
            registry = registry
        )

        withDevices(
            logger = logger,
            client = kubernetesReservation,
            configurationName = configuration.name,
            runType = runType,
            tests = testsToRun
        ) { devices ->
            val testRequests = testsToRun
                .map { targetTestRun ->
                    val reservation = when (runType) {
                        is TestExecutor.RunType.Run -> targetTestRun.target.reservation
                        is TestExecutor.RunType.Rerun -> targetTestRun.target.rerunReservation
                    }

                    val quota = reservation.quota

                    TestRunRequest(
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
                        application = application.absolutePath,
                        applicationPackage = executionParameters.applicationPackageName,
                        testApplication = testApplication.absolutePath,
                        testPackage = executionParameters.applicationTestPackageName,
                        testRunner = executionParameters.testRunner,
                        timeoutMinutes = TEST_TIMEOUT_MINUTES,
                        instrumentationParameters = targetTestRun.target.instrumentationParams
                    )
                }

            val runnerArguments = Arguments(
                outputDirectory = outputFolder(output),
                devices = devices,
                logger = object : Logger {
                    override fun notify(message: String, error: Throwable?) {
                        logger.critical(message, error)
                    }

                    override fun log(message: String) {
                        logger.info(message)
                    }
                },
                listener = testReporter,
                requests = testRequests
            )

            logger.debug("Arguments: $runnerArguments")

            runner.run(arguments = runnerArguments)
        }

        logger.info("Worker completed")
    }

    private fun outputFolder(output: File): File = File(
        output,
        outputDirectoryName
    ).apply { mkdirs() }
}

private const val TEST_TIMEOUT_MINUTES = 5L
