package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.report.Report
import com.avito.logger.LoggerFactory
import com.avito.report.model.TestStaticData
import com.avito.runner.config.InstrumentationConfigurationData
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.runner.ExecutionParameters
import com.avito.runner.scheduler.runner.TestExecutorFactory
import com.avito.runner.scheduler.runner.model.TestWithTarget
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import java.io.File

internal class TestsRunnerImplementation(
    private val testExecutorFactory: TestExecutorFactory,
    private val testReporterFactory: (Map<TestCase, TestStaticData>, File, Report) -> TestLifecycleListener,
    private val loggerFactory: LoggerFactory,
    private val executionParameters: ExecutionParameters,
    private val outputDir: File,
    private val instrumentationConfiguration: InstrumentationConfigurationData,
    private val metricsConfig: RunnerMetricsConfig,
    private val devicesProviderFactory: DevicesProviderFactory,
    private val tempLogcatDir: File,
    private val projectName: String,
    private val saveTestArtifactsToOutputs: Boolean,
    private val fetchLogcatForIncompleteTests: Boolean,
) : TestsRunner {

    override fun runTests(
        mainApk: File?,
        testApk: File,
        report: Report,
        testsToRun: List<TestWithTarget>
    ) {
        if (testsToRun.isEmpty()) {
            return
        }

        val testReporter = testReporterFactory.invoke(
            testsToRun.associate { testWithTarget ->
                TestCase(
                    className = testWithTarget.test.name.className,
                    methodName = testWithTarget.test.name.methodName,
                    deviceName = testWithTarget.target.deviceName
                ) to testWithTarget.test
            },
            tempLogcatDir,
            report
        )

        val executor = testExecutorFactory.createExecutor(
            devicesProviderFactory = devicesProviderFactory,
            testReporter = testReporter,
            configuration = instrumentationConfiguration,
            executionParameters = executionParameters,
            loggerFactory = loggerFactory,
            metricsConfig = metricsConfig,
            outputDir = outputDir,
            projectName = projectName,
            tempLogcatDir = tempLogcatDir,
            saveTestArtifactsToOutputs = saveTestArtifactsToOutputs,
            fetchLogcatForIncompleteTests = fetchLogcatForIncompleteTests,
        )

        executor.execute(
            application = mainApk,
            testApplication = testApk,
            testsToRun = testsToRun,
            executionParameters = executionParameters,
            output = outputDir
        )
    }
}
