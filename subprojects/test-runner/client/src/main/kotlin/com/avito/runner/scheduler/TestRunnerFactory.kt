package com.avito.runner.scheduler

import com.avito.android.stats.StatsDSender
import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.args.TestRunnerFactoryConfig
import com.avito.runner.scheduler.listener.ArtifactsTestListener
import com.avito.runner.scheduler.listener.LogListener
import com.avito.runner.scheduler.metrics.TestMetricsListenerImpl
import com.avito.runner.scheduler.metrics.TestMetricsSender
import com.avito.runner.scheduler.report.CompositeReporter
import com.avito.runner.scheduler.report.SummaryReportMakerImpl
import com.avito.runner.scheduler.report.trace.TraceReporter
import com.avito.runner.scheduler.runner.TestRunner
import com.avito.runner.scheduler.runner.TestRunnerExecutionState
import com.avito.runner.scheduler.runner.TestRunnerImpl
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.DeviceWorkerPoolImpl
import com.avito.runner.service.listener.CompositeListener
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.worker.device.Device
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.File

internal class TestRunnerFactory(
    private val outputDirectory: File,
    private val config: TestRunnerFactoryConfig
) {
    private val loggerFactory = config.loggerFactory
    private val timeProvider: TimeProvider = DefaultTimeProvider()
    private val statsDSender: StatsDSender = StatsDSender.Impl(
        config = config.metricsConfig.statsDConfig,
        loggerFactory = loggerFactory
    )
    private val testMetricsSender = TestMetricsListenerImpl(
        testMetricsSender = TestMetricsSender(
            statsDSender = statsDSender,
            prefix = config.metricsConfig.runnerPrefix
        ),
        timeProvider = timeProvider,
        loggerFactory = config.loggerFactory
    )

    fun createTestRunner(
        devices: ReceiveChannel<Device>
    ): TestRunner {
        val executionState = TestRunnerExecutionState()
        return TestRunnerImpl(
            scheduler = TestExecutionScheduler(
                results = executionState.results,
                intentions = executionState.intentions,
                intentionResults = executionState.intentionResults
            ),
            deviceWorkerPool = DeviceWorkerPoolImpl(
                outputDirectory = outputDirectory,
                timeProvider = timeProvider,
                loggerFactory = loggerFactory,
                testListener = setupListener(outputDirectory),
                deviceMetricsListener = testMetricsSender,
                state = DeviceWorkerPool.State(
                    devices = devices,
                    intentions = executionState.intentions,
                    intentionResults = executionState.intentionResults,
                    deviceSignals = executionState.deviceSignals,
                )
            ),
            reservationWatcher = DeviceReservationWatcher.Impl(
                reservation = config.reservation
            ),
            loggerFactory = loggerFactory,
            state = executionState,
            summaryReportMaker = SummaryReportMakerImpl(),
            reporter = CompositeReporter(
                reporters = setOf(
                    TraceReporter(
                        runName = "Tests",
                        outputDirectory = outputDirectory
                    )
                )
            ),
            testMetricsListener = testMetricsSender
        )
    }

    private fun setupListener(outputDirectory: File): TestListener =
        CompositeListener(
            listeners = mutableListOf<TestListener>().apply {
                add(LogListener())
                add(
                    ArtifactsTestListener(
                        lifecycleListener = config.listener,
                        outputDirectory = outputDirectory,
                        loggerFactory = loggerFactory,
                        saveTestArtifactsToOutputs = config.saveTestArtifactsToOutputs,
                        fetchLogcatForIncompleteTests = config.fetchLogcatForIncompleteTests,
                    )
                )
            }
        )
}
