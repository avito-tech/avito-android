package com.avito.runner.scheduler

import com.avito.runner.exit.GradleExitManager
import com.avito.runner.scheduler.args.Arguments
import com.avito.runner.scheduler.listener.ArtifactsTestListener
import com.avito.runner.scheduler.listener.LogListener
import com.avito.runner.scheduler.report.CompositeReporter
import com.avito.runner.scheduler.report.SummaryReportMakerImplementation
import com.avito.runner.scheduler.report.trace.TraceReporter
import com.avito.runner.scheduler.runner.TestRunnerImplementation
import com.avito.runner.scheduler.runner.client.TestExecutionClient
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.IntentionExecutionServiceImplementation
import com.avito.runner.service.listener.CompositeListener
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.runner.service.worker.device.observer.ExternalIntentionBasedDevicesObserver

class TestsRunnerClient {

    fun run(arguments: Arguments) {
        val exitManager = GradleExitManager()

        val testRunner = TestRunnerImplementation(
            scheduler = TestExecutionScheduler(
                logger = arguments.logger
            ),
            client = TestExecutionClient(),
            service = IntentionExecutionServiceImplementation(
                outputDirectory = arguments.outputDirectory,
                devicesObserver = ExternalIntentionBasedDevicesObserver(
                    devicesManager = AdbDevicesManager(
                        logger = arguments.logger
                    ),
                    externalIntentionOfSerials = arguments.devices,
                    exitManager = exitManager,
                    logger = arguments.logger
                ),
                logger = arguments.logger,
                listener = setupListener(arguments)
            ),
            logger = arguments.logger
        )

        Entrypoint(
            testRunner = testRunner,
            summaryReportMaker = SummaryReportMakerImplementation(),
            reporter = CompositeReporter(
                reporters = setOf(
                    TraceReporter(
                        runName = "Tests",
                        outputDirectory = arguments.outputDirectory
                    )
                )
            ),
            exitManager = exitManager,
            logger = arguments.logger,
            failOnFailedTests = arguments.failOnFailedTests
        ).also {
            it.run(
                requests = arguments.requests
            )
        }
    }

    private fun setupListener(arguments: Arguments): TestListener =
        CompositeListener(
            listeners = mutableListOf<TestListener>().apply {
                add(LogListener())

                if (arguments.listener != null) {
                    add(
                        ArtifactsTestListener(
                            lifecycleListener = arguments.listener,
                            logger = arguments.logger
                        )
                    )
                }
            }
        )
}
