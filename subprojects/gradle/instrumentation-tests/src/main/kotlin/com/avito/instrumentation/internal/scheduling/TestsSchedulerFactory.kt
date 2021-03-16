package com.avito.instrumentation.internal.scheduling

import com.avito.android.TestSuiteLoader
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.report.Report
import com.avito.android.stats.StatsDSender
import com.avito.filestorage.RemoteStorageFactory
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.executing.TestExecutorFactory
import com.avito.instrumentation.internal.report.listener.ReportViewerTestReporter
import com.avito.instrumentation.internal.suite.TestSuiteProvider
import com.avito.instrumentation.internal.suite.filter.FilterFactory
import com.avito.instrumentation.internal.suite.filter.FilterInfoWriter
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.retrace.ProguardRetracer
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import com.google.gson.Gson
import java.io.File
import java.nio.file.Files

internal interface TestsSchedulerFactory {

    fun create(devicesProviderFactory: DevicesProviderFactory): TestsScheduler

    class Impl(
        private val params: InstrumentationTestsAction.Params,
        private val sourceReport: Report,
        private val gson: Gson,
        private val timeProvider: TimeProvider,
        private val metricsConfig: RunnerMetricsConfig,
        private val testExecutorFactory: TestExecutorFactory,
        private val testSuiteLoader: TestSuiteLoader
    ) : TestsSchedulerFactory {

        override fun create(devicesProviderFactory: DevicesProviderFactory): TestsScheduler {
            val tempDir = Files.createTempDirectory(null).toFile()
            val testRunner: TestsRunner = createTestRunner(devicesProviderFactory, tempDir)
            val testSuiteProvider: TestSuiteProvider = createTestSuiteProvider()

            return InstrumentationTestsScheduler(
                testsRunner = testRunner,
                params = params,
                reportCoordinates = params.reportCoordinates,
                sourceReport = sourceReport,
                testSuiteProvider = testSuiteProvider,
                testSuiteLoader = testSuiteLoader,
                gson = gson,
                filterInfoWriter = FilterInfoWriter.Impl(
                    outputDir = params.outputDir,
                    gson = gson
                ),
                loggerFactory = params.loggerFactory
            )
        }

        private fun createTestSuiteProvider(): TestSuiteProvider = TestSuiteProvider.Impl(
            report = sourceReport,
            targets = params.instrumentationConfiguration.targets,
            filterFactory = FilterFactory.create(
                filterData = params.instrumentationConfiguration.filter,
                impactAnalysisResult = params.impactAnalysisResult,
                factory = params.reportFactory,
                reportConfig = params.reportConfig
            ),
            reportSkippedTests = params.instrumentationConfiguration.reportSkippedTests
        )

        private fun createTestRunner(devicesProviderFactory: DevicesProviderFactory, tempDir: File): TestsRunner =
            TestsRunnerImplementation(
                testExecutorFactory = testExecutorFactory,
                testReporterFactory = { testSuite, logcatDir, report ->
                    ReportViewerTestReporter(
                        loggerFactory = params.loggerFactory,
                        testSuite = testSuite,
                        report = report,
                        logcatDir = logcatDir,
                        retracer = ProguardRetracer.Impl(params.proguardMappings),
                        metricsSender = InstrumentationMetricsSender(
                            statsDSender = StatsDSender.Impl(
                                config = metricsConfig.statsDConfig,
                                loggerFactory = params.loggerFactory
                            ),
                            runnerPrefix = metricsConfig.runnerPrefix
                        ),
                        remoteStorage = RemoteStorageFactory.create(
                            endpoint = params.fileStorageUrl,
                            loggerFactory = params.loggerFactory,
                            timeProvider = timeProvider
                        )
                    )
                },
                loggerFactory = params.loggerFactory,
                executionParameters = params.executionParameters,
                outputDir = params.outputDir,
                instrumentationConfiguration = params.instrumentationConfiguration,
                metricsConfig = metricsConfig,
                devicesProviderFactory = devicesProviderFactory,
                tempLogcatDir = tempDir,
                projectName = params.projectName
            )
    }
}
