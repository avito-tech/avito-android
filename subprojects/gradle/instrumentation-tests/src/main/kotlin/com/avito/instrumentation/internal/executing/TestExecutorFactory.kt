package com.avito.instrumentation.internal.executing

import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.internal.reservation.devices.provider.DevicesProviderFactory
import com.avito.instrumentation.report.listener.TestReporter
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
                devicesProvider = devicesProviderFactory.create(configuration, executionParameters),
                testReporter = testReporter,
                buildId = buildId,
                configurationName = configuration.name,
                loggerFactory = loggerFactory,
                statsDConfig = statsDConfig
            )
        }
    }
}
