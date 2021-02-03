package com.avito.instrumentation.internal.executing

import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.internal.report.listener.TestReporter
import com.avito.logger.LoggerFactory

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
        statsDConfig: StatsDConfig
    ): TestExecutor

    class Implementation : TestExecutorFactory {

        override fun createExecutor(
            devicesProviderFactory: DevicesProviderFactory,
            testReporter: TestReporter,
            buildId: String,
            configuration: InstrumentationConfiguration.Data,
            executionParameters: ExecutionParameters,
            loggerFactory: LoggerFactory,
            statsDConfig: StatsDConfig
        ): TestExecutor {
            return TestExecutorImpl(
                devicesProvider = devicesProviderFactory.create(
                    deviceType = configuration.requestedDeviceType,
                    configurationName = configuration.name,
                    logcatTags = executionParameters.logcatTags,
                    kubernetesNamespace = executionParameters.namespace
                ),
                testReporter = testReporter,
                buildId = buildId,
                configurationName = configuration.name,
                loggerFactory = loggerFactory,
                statsDConfig = statsDConfig
            )
        }
    }
}
