package com.avito.runner.scheduler

import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.report.Report
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.scheduler.metrics.InstrumentationMetricsSender
import com.avito.runner.scheduler.metrics.TestMetricsListenerImpl
import com.avito.runner.scheduler.metrics.TestMetricsSender
import com.avito.runner.scheduler.runner.TestRunnerExecutionState
import com.avito.runner.scheduler.runner.model.TestRunRequestFactory
import com.avito.runner.service.DeviceWorkerPoolProvider
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.runner.service.worker.listener.DeviceListener
import com.avito.time.TimeProvider
import java.io.File
import java.nio.file.Files

public class TestRunnerFactoryProvider(
    private val params: RunnerInputParams,
    private val timeProvider: TimeProvider,
    private val httpClientProvider: HttpClientProvider,
    private val report: Report,
    private val devicesProviderFactory: DevicesProviderFactory,
    metricsConfig: RunnerMetricsConfig,
) {

    private val loggerFactory = params.loggerFactory
    private val outputDir = params.outputDir
    private val tempLogcatDir = Files.createTempDirectory(null).toFile()
    private val testRunnerExecutionState = TestRunnerExecutionState()

    private val statsDSender: StatsDSender = StatsDSender.Impl(
        config = metricsConfig.statsDConfig,
        loggerFactory = loggerFactory
    )

    private val metricsSender = InstrumentationMetricsSender(
        statsDSender = statsDSender,
        runnerPrefix = metricsConfig.runnerPrefix
    )

    private val testMetricsSender = TestMetricsListenerImpl(
        testMetricsSender = TestMetricsSender(
            statsDSender = statsDSender,
            prefix = metricsConfig.runnerPrefix
        ),
        timeProvider = timeProvider,
        loggerFactory = loggerFactory
    )

    /**
     * i.e {projectDir}/output/test-runner/
     */
    private val testRunnerOutputDir: File by lazy {
        File(
            outputDir,
            "test-runner"
        ).apply { mkdirs() }
    }

    internal fun provide(): TestRunnerFactory {
        return TestRunnerFactoryImpl(
            testRunnerOutputDir = testRunnerOutputDir,
            loggerFactory = loggerFactory,
            testMetricsListener = testMetricsSender,
            devicesProvider = devicesProviderFactory.create(
                tempLogcatDir,
                deviceWorkerPoolProvider(testMetricsSender)
            ),
            testRunnerRequestFactory = testRunRequestFactory(),
            executionState = testRunnerExecutionState,
            httpClientProvider = httpClientProvider,
            timeProvider = timeProvider,
            params = params,
            tempLogcatDir = tempLogcatDir,
            metricsSender = metricsSender,
            report = report
        )
    }

    private fun testRunRequestFactory(): TestRunRequestFactory {
        return TestRunRequestFactory(
            application = params.mainApk,
            testApplication = params.testApk,
            executionParameters = params.executionParameters
        )
    }

    private fun deviceWorkerPoolProvider(
        deviceListener: DeviceListener
    ): DeviceWorkerPoolProvider {
        return DeviceWorkerPoolProvider(
            testRunnerOutputDir = testRunnerOutputDir,
            timeProvider = timeProvider,
            loggerFactory = loggerFactory,
            deviceListener = deviceListener,
            intentions = testRunnerExecutionState.intentions,
            intentionResults = testRunnerExecutionState.intentionResults,
            deviceSignals = testRunnerExecutionState.deviceSignals
        )
    }
}
