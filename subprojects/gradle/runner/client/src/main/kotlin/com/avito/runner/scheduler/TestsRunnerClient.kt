package com.avito.runner.scheduler

import com.avito.android.stats.StatsDSender
import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.args.Arguments
import com.avito.runner.scheduler.listener.ArtifactsTestListener
import com.avito.runner.scheduler.listener.LogListener
import com.avito.runner.scheduler.metrics.TestMetricsListener
import com.avito.runner.scheduler.metrics.TestMetricsListenerImpl
import com.avito.runner.scheduler.metrics.TestMetricsSender
import com.avito.runner.scheduler.report.CompositeReporter
import com.avito.runner.scheduler.report.SummaryReportMakerImplementation
import com.avito.runner.scheduler.report.trace.TraceReporter
import com.avito.runner.scheduler.runner.TestRunnerImplementation
import com.avito.runner.scheduler.runner.client.TestExecutionClient
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.IntentionExecutionServiceImplementation
import com.avito.runner.service.listener.CompositeListener
import com.avito.runner.service.listener.TestListener
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider

class TestsRunnerClient {

    fun run(arguments: Arguments) {

        val loggerFactory = arguments.loggerFactory

        val timeProvider: TimeProvider = DefaultTimeProvider(loggerFactory)

        val statsDSender: StatsDSender = StatsDSender.Impl(
            config = arguments.statsDConfig,
            loggerFactory = loggerFactory
        )

        val testMetricsSender: TestMetricsListener = TestMetricsListenerImpl(
            testMetricsSender = TestMetricsSender(
                statsDSender = statsDSender,
                buildId = arguments.buildId,
                instrumentationConfigName = arguments.instrumentationConfigName
            ),
            timeProvider = timeProvider,
            loggerFactory = arguments.loggerFactory
        )

        testMetricsSender.onTestSuiteStarted()

        val testRunner = TestRunnerImplementation(
            scheduler = TestExecutionScheduler(
                loggerFactory = loggerFactory
            ),
            client = TestExecutionClient(
                loggerFactory = loggerFactory
            ),
            service = IntentionExecutionServiceImplementation(
                outputDirectory = arguments.outputDirectory,
                devices = arguments.devices,
                loggerFactory = loggerFactory,
                listener = setupListener(
                    arguments = arguments,
                    testMetricsListener = testMetricsSender
                )
            ),
            reservationWatcher = DeviceReservationWatcher.Impl(
                reservation = arguments.reservation
            ),
            loggerFactory = loggerFactory
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
            loggerFactory = loggerFactory
        ).also {
            it.run(
                requests = arguments.requests
            )
        }

        testMetricsSender.onTestSuiteFinished()
    }

    private fun setupListener(
        arguments: Arguments,
        testMetricsListener: TestMetricsListener
    ): TestListener = CompositeListener(
        listeners = mutableListOf<TestListener>().apply {
            add(LogListener())
            add(
                ArtifactsTestListener(
                    lifecycleListener = arguments.listener,
                    loggerFactory = arguments.loggerFactory
                )
            )
            add(testMetricsListener)
        }
    )
}
