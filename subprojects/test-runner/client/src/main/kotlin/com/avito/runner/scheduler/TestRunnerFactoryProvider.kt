package com.avito.runner.scheduler

import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.stats.StatsDSender
import com.avito.logger.LoggerFactory
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.scheduler.metrics.InstrumentationMetricsSender
import com.avito.runner.scheduler.metrics.TestRunnerMetricsListener
import com.avito.runner.scheduler.metrics.TestRunnerMetricsSenderImpl
import com.avito.runner.scheduler.report.ReportModule
import com.avito.runner.scheduler.runner.TestRunnerExecutionState
import com.avito.runner.scheduler.runner.model.TestRunRequestFactory
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import java.io.File

internal class TestRunnerFactoryProvider(
    private val params: RunnerInputParams,
    private val timeProvider: TimeProvider,
    private val reportModule: ReportModule,
    private val devicesProviderFactory: DevicesProviderFactory,
    private val loggerFactory: LoggerFactory,
    private val testRunnerOutputDir: File,
    private val tempLogcatDir: File,
    metricsConfig: RunnerMetricsConfig,
) {
    private val testRunnerExecutionState = TestRunnerExecutionState()

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
            targets = params.instrumentationConfiguration.targets,
            metricsSender = metricsSender,
            artifactsTestListenerProvider = reportModule.artifactsTestListenerProvider
        )
    }

    private fun testRunRequestFactory(): TestRunRequestFactory {
        return TestRunRequestFactory(
            application = params.mainApk,
            testApplication = params.testApk,
            deviceDebug = params.deviceDebug,
            executionParameters = params.executionParameters,
            targets = params.instrumentationConfiguration.targets.associateBy { it.deviceName },
        )
    }
}
