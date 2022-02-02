package com.avito.runner.scheduler

import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.stats.StatsDSender
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.report.Report
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.listener.TestListenerFactory
import com.avito.runner.scheduler.metrics.InstrumentationMetricsSender
import com.avito.runner.scheduler.metrics.TestRunnerMetricsListener
import com.avito.runner.scheduler.metrics.TestRunnerMetricsSenderImpl
import com.avito.runner.scheduler.runner.TestRunnerExecutionState
import com.avito.runner.scheduler.runner.model.TestRunRequestFactory
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.nio.file.Files

public class TestRunnerFactoryProvider(
    private val params: RunnerInputParams,
    private val timeProvider: TimeProvider,
    private val httpClientProvider: HttpClientProvider,
    private val report: Report,
    private val devicesProviderFactory: DevicesProviderFactory,
    private val loggerFactory: LoggerFactory,
    private val deviceSignals: Channel<Device.Signal>,
    metricsConfig: RunnerMetricsConfig,
) {

    private val outputDir = params.outputDir
    private val tempLogcatDir = Files.createTempDirectory(null).toFile()
    private val testRunnerExecutionState = TestRunnerExecutionState(deviceSignals = deviceSignals)

    private val statsDSender: StatsDSender = StatsDSender.create(
        config = metricsConfig.statsDConfig,
        loggerFactory = loggerFactory
    )

    private val metricsSender = InstrumentationMetricsSender(
        statsDSender = statsDSender,
        runnerPrefix = metricsConfig.runnerPrefix
    )

    private val testMetricsSender = TestRunnerMetricsListener(
        testMetricsSender = TestRunnerMetricsSenderImpl(
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
            timeProvider = timeProvider,
            loggerFactory = loggerFactory,
            testSuiteListener = testMetricsSender,
            deviceListener = testMetricsSender,
            devicesProviderFactory = devicesProviderFactory,
            testRunnerRequestFactory = testRunRequestFactory(),
            executionState = testRunnerExecutionState,
            params = params,
            tempLogcatDir = tempLogcatDir,
            testListenerFactory = TestListenerFactory(
                loggerFactory = loggerFactory,
                timeProvider = timeProvider,
                httpClientProvider = httpClientProvider
            ),
            report = report,
            targets = params.instrumentationConfiguration.targets,
            metricsSender = metricsSender,
        )
    }

    private fun testRunRequestFactory(): TestRunRequestFactory {
        return TestRunRequestFactory(
            application = params.mainApk,
            testApplication = params.testApk,
            deviceDebug = params.deviceDebug,
            executionParameters = params.executionParameters,
            targets = params.instrumentationConfiguration.targets.associateBy { it.deviceName }
        )
    }
}
