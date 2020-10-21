package com.avito.instrumentation.executing

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.report.listener.TestReporter
import com.avito.instrumentation.reservation.devices.provider.DevicesProviderFactory
import com.avito.utils.logging.CILogger

/**
 * Abstraction for testing purposes only
 */
interface TestExecutorFactory {

    fun createExecutor(
        devicesProviderFactory: DevicesProviderFactory,
        testReporter: TestReporter,
        configuration: InstrumentationConfiguration.Data,
        executionParameters: ExecutionParameters,
        logger: CILogger
    ): TestExecutor

    class Implementation : TestExecutorFactory {

        override fun createExecutor(
            devicesProviderFactory: DevicesProviderFactory,
            testReporter: TestReporter,
            configuration: InstrumentationConfiguration.Data,
            executionParameters: ExecutionParameters,
            logger: CILogger
        ): TestExecutor {
            return TestExecutor.Impl(
                devicesProvider = devicesProviderFactory.create(configuration, executionParameters),
                testReporter = testReporter,
                configurationName = configuration.name,
                logger = logger
            )
        }
    }
}
