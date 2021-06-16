package com.avito.runner.scheduler

import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.args.TestRunnerFactoryConfig
import com.avito.runner.scheduler.metrics.TestMetricsListener
import com.avito.runner.scheduler.report.CompositeReporter
import com.avito.runner.scheduler.report.SummaryReportMakerImpl
import com.avito.runner.scheduler.report.trace.TraceReporter
import com.avito.runner.scheduler.runner.TestRunner
import com.avito.runner.scheduler.runner.TestRunnerExecutionState
import com.avito.runner.scheduler.runner.TestRunnerImpl
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.DeviceWorkerPoolProvider
import com.avito.runner.service.worker.device.Device
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.File

internal class TestRunnerFactory(
    outputDir: File,
    private val config: TestRunnerFactoryConfig,
    private val deviceWorkerPoolProvider: DeviceWorkerPoolProvider,
    private val testMetricsListener: TestMetricsListener
) {
    private val loggerFactory = config.loggerFactory

    /**
     * i.e {projectDir}/output/test-runner/
     */
    private val testRunnerOutputDir: File by lazy {
        File(
            outputDir,
            "test-runner"
        ).apply { mkdirs() }
    }

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
            deviceWorkerPool = deviceWorkerPoolProvider.provide(
                testRunnerOutputDir = testRunnerOutputDir,
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
                        outputDirectory = testRunnerOutputDir
                    )
                )
            ),
            testMetricsListener = testMetricsListener
        )
    }
}
