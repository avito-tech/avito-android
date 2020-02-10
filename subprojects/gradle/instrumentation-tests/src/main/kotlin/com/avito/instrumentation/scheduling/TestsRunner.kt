package com.avito.instrumentation.scheduling

import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.executing.ExecutionParameters
import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.executing.TestExecutorFactory
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.report.listener.TestReporter
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.instrumentation.suite.model.transformTestsWithNewJobSlug
import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticData
import com.avito.runner.service.model.TestCase
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.logging.CILogger
import org.funktionale.tries.Try
import java.io.File
import java.nio.file.Files

interface TestsRunner {

    fun runTests(
        mainApk: File,
        testApk: File,
        runType: TestExecutor.RunType,
        reportCoordinates: ReportCoordinates,
        report: Report,
        testsToRun: List<TestWithTarget>,
        currentReportState: () -> Try<List<SimpleRunTest>>
    ): Try<List<SimpleRunTest>>
}

class TestsRunnerImplementation(
    private val testExecutorFactory: TestExecutorFactory,
    private val kubernetesCredentials: KubernetesCredentials,
    private val testReporterFactory: (Map<TestCase, TestStaticData>, File, Report) -> TestReporter?,
    private val logger: CILogger,
    private val buildId: String,
    private val projectName: String,
    private val executionParameters: ExecutionParameters,
    private val outputDirectory: File,
    private val instrumentationConfiguration: InstrumentationConfiguration.Data,
    private val reportsApi: ReportsApi,
    private val registry: String
) : TestsRunner {

    override fun runTests(
        mainApk: File,
        testApk: File,
        runType: TestExecutor.RunType,
        reportCoordinates: ReportCoordinates,
        report: Report,
        testsToRun: List<TestWithTarget>,
        currentReportState: () -> Try<List<SimpleRunTest>>
    ): Try<List<SimpleRunTest>> {
        return if (testsToRun.isEmpty()) {
            //todo есть сомнения что это нормально работает во всех кейсах, нужно больше тестов
            currentReportState.invoke().rescue { Try.Success(emptyList()) }
        } else {

            val output = File(outputDirectory, runType.id).apply { mkdirs() }
            val logcatDir = Files.createTempDirectory(null).toFile()

            testExecutorFactory.createExecutor(
                logger = logger.child(runType.id),
                kubernetesCredentials = kubernetesCredentials,
                buildId = buildId,
                projectName = projectName,
                testReporter = testReporterFactory.invoke(
                    testsToRun.associate {
                        TestCase(
                            className = it.test.name.className,
                            methodName = it.test.name.methodName,
                            deviceName = it.target.deviceName
                        ) to it.test
                    },
                    logcatDir,
                    report
                ),
                registry = registry
            ).execute(
                application = mainApk,
                testApplication = testApk,
                testsToRun = testsToRun.transformTestsWithNewJobSlug(reportCoordinates.jobSlug),
                executionParameters = executionParameters,
                configuration = instrumentationConfiguration.copy(name = "${instrumentationConfiguration.name}-${runType.id}"),
                output = output,
                runType = runType,
                logcatDir = logcatDir
            )

            //todo через Report
            reportsApi.getTestsForRunId(reportCoordinates)
        }
    }
}
