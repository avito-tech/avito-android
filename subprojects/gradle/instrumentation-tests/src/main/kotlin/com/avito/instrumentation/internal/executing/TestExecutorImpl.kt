package com.avito.instrumentation.internal.executing

import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.internal.reservation.devices.provider.DevicesProvider
import com.avito.instrumentation.report.listener.TestReporter
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.scheduler.TestsRunnerClient
import com.avito.runner.scheduler.args.Arguments
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import java.io.File

internal class TestExecutorImpl(
  private val devicesProvider: DevicesProvider,
  private val testReporter: TestReporter,
  private val buildId: String,
  private val configurationName: String,
  private val loggerFactory: LoggerFactory,
  private val statsDConfig: StatsDConfig
) : TestExecutor {

    private val logger = loggerFactory.create<TestExecutor>()

    private val runner = TestsRunnerClient()

    private val outputDirectoryName = "test-runner"

    override fun execute(
        application: File?,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        output: File
    ) {
        withDevices(reservations = reservations(testsToRun)) { devices ->

            val testRequests = testsToRun.map { targetTestRun: TestWithTarget ->

                val reservation = targetTestRun.target.reservation

                createTestRunRequest(
                    targetTestRun = targetTestRun,
                    quota = reservation.quota,
                    reservation = reservation,
                    application = application,
                    testApplication = testApplication,
                    executionParameters = executionParameters
                )
            }

            val runnerArguments = Arguments(
              outputDirectory = outputFolder(output),
              buildId = buildId,
              instrumentationConfigName = configurationName,
              devices = devices,
              loggerFactory = loggerFactory,
              listener = testReporter,
              requests = testRequests,
              reservation = devicesProvider,
              statsDConfig = statsDConfig
            )

            logger.debug("Arguments: $runnerArguments")

            runner.run(arguments = runnerArguments)
        }

        logger.debug("Worker completed")
    }

    private fun outputFolder(output: File): File = File(
      output,
      outputDirectoryName
    ).apply { mkdirs() }

    private fun reservations(
        tests: List<TestWithTarget>
    ): Collection<Reservation.Data> {

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
    // It's overcomplicated for local client
    private fun withDevices(
      reservations: Collection<Reservation.Data>,
      action: (devices: ReceiveChannel<Device>) -> Unit
    ) {
      runBlocking {
        try {
          logger.info("Devices: Starting action job for configuration: $configurationName...")
          action(devicesProvider.provideFor(reservations, this))
          logger.info("Devices: Action completed for configuration: $configurationName")
        } catch (e: Throwable) {
          logger.critical("Error during action in $configurationName job", e)
        } finally {
          logger.info("Devices: Starting releasing devices for configuration: $configurationName...")
          devicesProvider.releaseDevices()
          logger.info("Devices: Devices released for configuration: $configurationName")
        }
      }
    }

    private fun createTestRunRequest(
      targetTestRun: TestWithTarget,
      quota: QuotaConfiguration.Data,
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

    data class TargetGroup(val name: String, val reservation: Reservation)
}

private const val TEST_TIMEOUT_MINUTES = 5L
