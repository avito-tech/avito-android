package com.avito.runner.scheduler

import com.avito.runner.reservation.DeviceReservationWatcher
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

class TestsRunnerClient {

    fun run(arguments: Arguments) {

        val testRunner = TestRunnerImplementation(
            scheduler = TestExecutionScheduler(
                logger = arguments.logger
            ),
            client = TestExecutionClient(),
            service = IntentionExecutionServiceImplementation(
                outputDirectory = arguments.outputDirectory,
                devices = arguments.devices,
                logger = arguments.logger,
                listener = setupListener(arguments)
            ),
            reservationWatcher = DeviceReservationWatcher.Impl(arguments.reservation),
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
            logger = arguments.logger
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
                add(
                    ArtifactsTestListener(
                        lifecycleListener = arguments.listener,
                        logger = arguments.logger
                    )
                )
            }
        )
}
