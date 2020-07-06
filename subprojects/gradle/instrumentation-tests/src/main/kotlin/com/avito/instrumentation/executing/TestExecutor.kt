package com.avito.instrumentation.executing

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.report.listener.TestReporter
import com.avito.instrumentation.reservation.client.ReservationClient
import com.avito.instrumentation.reservation.client.ReservationClientFactory
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.instrumentation.util.launchGroupedCoroutines
import com.avito.runner.logging.Logger
import com.avito.runner.scheduler.TestsRunnerClient
import com.avito.runner.scheduler.args.Arguments
import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.utils.logging.CILogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import java.io.File

interface TestExecutor {

    fun execute(
        application: File?,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        configuration: InstrumentationConfiguration.Data,
        runType: RunType,
        output: File,
        logcatDir: File
    )

    data class RunType(val id: String)

    class Impl(
        private val logger: CILogger,
        private val reservationClientFactory: ReservationClientFactory,
        private val testReporter: TestReporter?
    ) : TestExecutor {

        private val runner = TestsRunnerClient()

        //todo hi Juno!
        private val outputDirectoryName = "composer"

        override fun execute(
            application: File?,
            testApplication: File,
            testsToRun: List<TestWithTarget>,
            executionParameters: ExecutionParameters,
            configuration: InstrumentationConfiguration.Data,
            runType: RunType,
            output: File,
            logcatDir: File
        ) {
            val reservationClient = reservationClientFactory.create(
                configuration = configuration,
                executionParameters = executionParameters
            )
            val reservations = reservations(
                testsToRun,
                configurationName = configuration.name
            )
            withDevices(
                client = reservationClient,
                configurationName = configuration.name,
                reservations = reservations
            ) { devices ->
                val testRequests = testsToRun
                    .map { targetTestRun ->
                        val reservation = targetTestRun.target.reservation

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
                            application = application?.absolutePath,
                            applicationPackage = executionParameters.applicationPackageName,
                            testApplication = testApplication.absolutePath,
                            testPackage = executionParameters.applicationTestPackageName,
                            testRunner = executionParameters.testRunner,
                            timeoutMinutes = TEST_TIMEOUT_MINUTES,
                            instrumentationParameters = targetTestRun.target.instrumentationParams,
                            enableDeviceDebug = executionParameters.enableDeviceDebug
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

        private fun reservations(
            tests: List<TestWithTarget>,
            configurationName: String
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
            client: ReservationClient,
            configurationName: String,
            reservations: Collection<Reservation.Data>,
            action: (devices: Channel<Serial>) -> Unit
        ) {
            val reservationDeployments = Channel<String>(reservations.size)
            try {
                val serialsChannel = Channel<Serial>(Channel.UNLIMITED)

                launchGroupedCoroutines {
                    launch(blocking = false) {
                        logger.info("Devices: Starting reservation job for configuration: $configurationName...")
                        client.claim(
                            reservations = reservations,
                            serialsChannel = serialsChannel,
                            reservationDeployments = reservationDeployments
                        )
                        logger.info("Devices: Reservation job completed for configuration: $configurationName")
                    }
                    launch {
                        logger.info("Devices: Starting action for configuration: $configurationName...")
                        action(serialsChannel)
                        logger.info("Devices: Action completed for configuration: $configurationName")
                    }
                }

            } catch (e: Throwable) {
                logger.critical("Error during starting reservation job", e)
            } finally {
                reservationDeployments.close()
                logger.info("Devices: Starting releasing devices for configuration: $configurationName...")
                runBlocking {
                    client.release(reservationDeployments = reservationDeployments.toList())
                }
                logger.info("Devices: Devices released for configuration: $configurationName")
            }
        }

        data class TargetGroup(val name: String, val reservation: Reservation)
    }
}

private const val TEST_TIMEOUT_MINUTES = 5L
