package com.avito.instrumentation.internal.executing

import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.internal.report.listener.TestReporter
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig

/**
 * Abstraction for testing purposes only
 */
internal interface TestExecutorFactory {

    fun createExecutor(
        devicesProviderFactory: DevicesProviderFactory,
        testReporter: TestReporter,
        buildId: String,
        configuration: InstrumentationConfiguration.Data,
        executionParameters: ExecutionParameters,
        loggerFactory: LoggerFactory,
        metricsConfig: RunnerMetricsConfig
    ): TestExecutor

    class Implementation : TestExecutorFactory {

        override fun createExecutor(
            devicesProviderFactory: DevicesProviderFactory,
            testReporter: TestReporter,
            buildId: String,
            configuration: InstrumentationConfiguration.Data,
            executionParameters: ExecutionParameters,
            loggerFactory: LoggerFactory,
            metricsConfig: RunnerMetricsConfig
        ): TestExecutor {
            return TestExecutorImpl(
                devicesProvider = devicesProviderFactory.create(
                    deviceType = configuration.requestedDeviceType,
                    configurationName = configuration.name,
                    logcatTags = executionParameters.logcatTags,
                    kubernetesNamespace = executionParameters.namespace
                ),
                testReporter = testReporter,
                configurationName = configuration.name,
                loggerFactory = loggerFactory,
                metricsConfig = metricsConfig
            )
        }
    }
}
