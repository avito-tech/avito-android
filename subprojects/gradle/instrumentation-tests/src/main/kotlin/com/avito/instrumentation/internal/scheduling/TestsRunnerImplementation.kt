package com.avito.instrumentation.internal.scheduling

import com.avito.android.stats.StatsDConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.executing.TestExecutor
import com.avito.instrumentation.internal.executing.TestExecutorFactory
import com.avito.instrumentation.internal.report.listener.TestReporter
import com.avito.instrumentation.internal.reservation.devices.provider.DevicesProviderFactory
import com.avito.instrumentation.internal.suite.model.TestWithTarget
import com.avito.instrumentation.internal.suite.model.transformTestsWithNewJobSlug
import com.avito.instrumentation.report.Report
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.runner.service.model.TestCase
import com.avito.time.TimeProvider
import com.avito.utils.gradle.KubernetesCredentials
import org.funktionale.tries.Try
import java.io.File
import java.nio.file.Files

internal class TestsRunnerImplementation(
    private val testExecutorFactory: TestExecutorFactory,
    private val kubernetesCredentials: KubernetesCredentials,
    private val testReporterFactory: (Map<TestCase, TestStaticData>, File, Report) -> TestReporter,
    private val loggerFactory: LoggerFactory,
    private val buildId: String,
    private val buildType: String,
    private val projectName: String,
    private val executionParameters: ExecutionParameters,
    private val outputDirectory: File,
    private val instrumentationConfiguration: InstrumentationConfiguration.Data,
    private val registry: String,
    private val statsDConfig: StatsDConfig,
    private val timeProvider: TimeProvider
) : TestsRunner {

    private val logger = loggerFactory.create<TestsRunner>()

    override fun runTests(
        mainApk: File?,
        testApk: File,
        runType: TestExecutor.RunType, // todo delete runtype
        reportCoordinates: ReportCoordinates,
        report: Report,
        testsToRun: List<TestWithTarget>
    ): Try<List<SimpleRunTest>> {
        return if (testsToRun.isEmpty()) {
            Try.Success(emptyList())
        } else {

            val output = File(outputDirectory, runType.id).apply { mkdirs() }
            val logcatDir = Files.createTempDirectory(null).toFile()

            val testReporter = testReporterFactory.invoke(
                testsToRun.associate {
                    TestCase(
                        className = it.test.name.className,
                        methodName = it.test.name.methodName,
                        deviceName = it.target.deviceName
                    ) to it.test
                },
                logcatDir,
                report
            )

            // TODO: pass through constructor
            val initialRunConfiguration =
                instrumentationConfiguration.copy(name = "${instrumentationConfiguration.name}-${runType.id}")

            val executor = testExecutorFactory.createExecutor(
                devicesProviderFactory = DevicesProviderFactory.Impl(
                    kubernetesCredentials = kubernetesCredentials,
                    buildId = buildId,
                    buildType = buildType,
                    projectName = projectName,
                    registry = registry,
                    output = output,
                    logcatDir = logcatDir,
                    loggerFactory = loggerFactory,
                    timeProvider = timeProvider
                ),
                configuration = initialRunConfiguration,
                executionParameters = executionParameters,
                testReporter = testReporter,
                buildId = buildId,
                loggerFactory = loggerFactory,
                statsDConfig = statsDConfig
            )

            executor.execute(
                application = mainApk,
                testApplication = testApk,
                testsToRun = testsToRun.transformTestsWithNewJobSlug(reportCoordinates.jobSlug),
                executionParameters = executionParameters,
                output = output
            )

            // todo через Report
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
