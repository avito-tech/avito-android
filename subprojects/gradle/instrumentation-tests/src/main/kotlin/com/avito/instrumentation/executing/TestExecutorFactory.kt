package com.avito.instrumentation.executing

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.report.listener.TestReporter
import com.avito.instrumentation.reservation.devices.provider.DevicesProviderFactory
import com.avito.logger.LoggerFactory

/**
 * Abstraction for testing purposes only
 */
interface TestExecutorFactory {

    fun createExecutor(
        devicesProviderFactory: DevicesProviderFactory,
        testReporter: TestReporter,
        configuration: InstrumentationConfiguration.Data,
        executionParameters: ExecutionParameters,
        loggerFactory: LoggerFactory
    ): TestExecutor

    class Implementation : TestExecutorFactory {

        override fun createExecutor(
            devicesProviderFactory: DevicesProviderFactory,
            testReporter: TestReporter,
            configuration: InstrumentationConfiguration.Data,
            executionParameters: ExecutionParameters,
            loggerFactory: LoggerFactory
        ): TestExecutor {
            return TestExecutor.Impl(
                devicesProvider = devicesProviderFactory.create(configuration, executionParameters),
                testReporter = testReporter,
                configurationName = configuration.name,
                loggerFactory = loggerFactory
            )
        }
    }
}
