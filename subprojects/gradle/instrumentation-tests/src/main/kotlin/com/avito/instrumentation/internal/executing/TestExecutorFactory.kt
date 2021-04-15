package com.avito.instrumentation.internal.executing

import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.logger.LoggerFactory
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import java.io.File

/**
 * Abstraction for testing purposes only
 */
internal interface TestExecutorFactory {

    fun createExecutor(
        devicesProviderFactory: DevicesProviderFactory,
        testReporter: TestLifecycleListener,
        configuration: InstrumentationConfiguration.Data,
        executionParameters: ExecutionParameters,
        loggerFactory: LoggerFactory,
        metricsConfig: RunnerMetricsConfig,
        outputDir: File,
        projectName: String,
        tempLogcatDir: File
    ): TestExecutor

    class Implementation : TestExecutorFactory {

        override fun createExecutor(
            devicesProviderFactory: DevicesProviderFactory,
            testReporter: TestLifecycleListener,
            configuration: InstrumentationConfiguration.Data,
            executionParameters: ExecutionParameters,
            loggerFactory: LoggerFactory,
            metricsConfig: RunnerMetricsConfig,
            outputDir: File,
            projectName: String,
            tempLogcatDir: File
        ): TestExecutor {
            return TestExecutorImpl(
                devicesProvider = devicesProviderFactory.create(
                    deviceType = configuration.requestedDeviceType,
                    projectName = projectName,
                    tempLogcatDir = tempLogcatDir,
                    outputDir = outputDir,
                    configurationName = configuration.name,
                    logcatTags = executionParameters.logcatTags,
                    kubernetesNamespace = executionParameters.namespace,
                    runnerPrefix = metricsConfig.runnerPrefix
                ),
                testReporter = testReporter,
                configurationName = configuration.name,
                loggerFactory = loggerFactory,
                metricsConfig = metricsConfig
            )
        }
    }
}
