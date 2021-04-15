package com.avito.instrumentation.internal.scheduling

import com.avito.android.Result
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.report.Report
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.executing.TestExecutorFactory
import com.avito.instrumentation.internal.suite.model.TestWithTarget
import com.avito.instrumentation.internal.suite.model.transformTestsWithNewJobSlug
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import java.io.File

internal class TestsRunnerImplementation(
    private val testExecutorFactory: TestExecutorFactory,
    private val testReporterFactory: (Map<TestCase, TestStaticData>, File, Report) -> TestLifecycleListener,
    private val loggerFactory: LoggerFactory,
    private val executionParameters: ExecutionParameters,
    private val outputDir: File,
    private val instrumentationConfiguration: InstrumentationConfiguration.Data,
    private val metricsConfig: RunnerMetricsConfig,
    private val devicesProviderFactory: DevicesProviderFactory,
    private val tempLogcatDir: File,
    private val projectName: String
) : TestsRunner {

    private val logger = loggerFactory.create<TestsRunner>()

    override fun runTests(
        mainApk: File?,
        testApk: File,
        reportCoordinates: ReportCoordinates,
        report: Report,
        testsToRun: List<TestWithTarget>
    ): Result<List<SimpleRunTest>> {
        return if (testsToRun.isEmpty()) {
            Result.Success(emptyList())
        } else {

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
                tempLogcatDir = tempLogcatDir
            )

            executor.execute(
                application = mainApk,
                testApplication = testApk,
                testsToRun = testsToRun.transformTestsWithNewJobSlug(reportCoordinates.jobSlug),
                executionParameters = executionParameters,
                output = outputDir
            )

            // todo through Report
            val raw = report.getTests()

            logger.debug("test results: $raw")

            val filtered = raw.map { runs ->
                runs.filterNotRelatedRunsToThisInstrumentation(testsToRun)
            }

            logger.debug("filtered results: $filtered")

            filtered
        }
    }

    private fun List<SimpleRunTest>.filterNotRelatedRunsToThisInstrumentation(
        testsToRun: List<TestWithTarget>
    ): List<SimpleRunTest> {
        return filter { run -> run.isRelatedTo(testsToRun) }
    }

    private fun SimpleRunTest.isRelatedTo(testsToRun: List<TestWithTarget>): Boolean {
        return testsToRun.any { testWithTarget ->
            testWithTarget.test.name.name == name && testWithTarget.target.deviceName == deviceName
        }
    }
}
