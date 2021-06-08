package com.avito.runner.scheduler.runner

import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.logger.LoggerFactory
import com.avito.runner.config.InstrumentationConfigurationData
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import java.io.File

/**
 * Abstraction for testing purposes only
 */
public interface TestExecutorFactory {

    public fun createExecutor(
        devicesProviderFactory: DevicesProviderFactory,
        testReporter: TestLifecycleListener,
        configuration: InstrumentationConfigurationData,
        executionParameters: ExecutionParameters,
        loggerFactory: LoggerFactory,
        metricsConfig: RunnerMetricsConfig,
        outputDir: File,
        projectName: String,
        tempLogcatDir: File,
        saveTestArtifactsToOutputs: Boolean,
        fetchLogcatForIncompleteTests: Boolean,
    ): TestExecutor

    public class Implementation : TestExecutorFactory {

        override fun createExecutor(
            devicesProviderFactory: DevicesProviderFactory,
            testReporter: TestLifecycleListener,
            configuration: InstrumentationConfigurationData,
            executionParameters: ExecutionParameters,
            loggerFactory: LoggerFactory,
            metricsConfig: RunnerMetricsConfig,
            outputDir: File,
            projectName: String,
            tempLogcatDir: File,
            saveTestArtifactsToOutputs: Boolean,
            fetchLogcatForIncompleteTests: Boolean,
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
                metricsConfig = metricsConfig,
                saveTestArtifactsToOutputs = saveTestArtifactsToOutputs,
                fetchLogcatForIncompleteTests = fetchLogcatForIncompleteTests,
            )
        }
    }
}
