package com.avito.instrumentation.internal.scheduling

import com.avito.android.TestSuiteLoader
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.report.Report
import com.avito.android.stats.StatsDSender
import com.avito.filestorage.RemoteStorageFactory
import com.avito.http.HttpClientProvider
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.executing.TestExecutorFactory
import com.avito.instrumentation.internal.report.listener.AvitoFileStorageUploader
import com.avito.instrumentation.internal.report.listener.LegacyTestArtifactsProcessor
import com.avito.instrumentation.internal.report.listener.LogcatProcessor
import com.avito.instrumentation.internal.report.listener.LogcatTestLifecycleListener
import com.avito.instrumentation.internal.report.listener.ReportProcessor
import com.avito.instrumentation.internal.report.listener.ReportProcessorImpl
import com.avito.instrumentation.internal.report.listener.TestArtifactsProcessor
import com.avito.instrumentation.internal.report.listener.TestArtifactsProcessorImpl
import com.avito.instrumentation.internal.report.listener.TestArtifactsUploader
import com.avito.instrumentation.internal.suite.TestSuiteProvider
import com.avito.instrumentation.internal.suite.filter.FilterFactory
import com.avito.instrumentation.internal.suite.filter.FilterInfoWriter
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.report.model.TestStaticData
import com.avito.retrace.ProguardRetracer
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.nio.file.Files

internal interface TestsSchedulerFactory {

    fun create(devicesProviderFactory: DevicesProviderFactory): TestsScheduler

    class Impl(
        private val params: InstrumentationTestsAction.Params,
        private val sourceReport: Report,
        private val gson: Gson,
        private val timeProvider: TimeProvider,
        private val httpClientProvider: HttpClientProvider,
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
                factoryLegacy = params.legacyReportFactory,
                legacyReportConfig = params.legacyReportConfig
            ),
            reportSkippedTests = params.instrumentationConfiguration.reportSkippedTests,
            useInMemoryReport = params.useInMemoryReport,
            timeProvider = timeProvider
        )

        private fun createTestRunner(devicesProviderFactory: DevicesProviderFactory, tempDir: File): TestsRunner {

            val statsDSender: StatsDSender = StatsDSender.Impl(
                config = metricsConfig.statsDConfig,
                loggerFactory = params.loggerFactory
            )

            val metricsSender = InstrumentationMetricsSender(
                statsDSender = statsDSender,
                runnerPrefix = metricsConfig.runnerPrefix
            )

            return TestsRunnerImplementation(
                testExecutorFactory = testExecutorFactory,
                testReporterFactory = { testSuite, logcatDir, report ->
                    LogcatTestLifecycleListener(
                        logcatDir = logcatDir,
                        reportProcessor = createReportProcessor(testSuite, metricsSender),
                        report = report,
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

        private fun createReportProcessor(
            testSuite: Map<TestCase, TestStaticData>,
            metricsSender: InstrumentationMetricsSender
        ): ReportProcessor {

            val dispatcher = Dispatchers.IO

            val retracer: ProguardRetracer = ProguardRetracer.Impl(params.proguardMappings)

            val artifactsUploader: TestArtifactsUploader = AvitoFileStorageUploader(
                RemoteStorageFactory.create(
                    endpoint = params.fileStorageUrl,
                    httpClientProvider = httpClientProvider,
                    loggerFactory = params.loggerFactory,
                    timeProvider = timeProvider
                )
            )

            val logcatUploader = LogcatProcessor.Impl(
                testArtifactsUploader = artifactsUploader,
                retracer = retracer
            )

            return ReportProcessorImpl(
                loggerFactory = params.loggerFactory,
                testSuite = testSuite,
                metricsSender = metricsSender,
                testArtifactsProcessor = createTestArtifactsProcessor(
                    uploadTestArtifacts = params.uploadTestArtifacts,
                    gson = TestArtifactsProcessor.gson,
                    dispatcher = dispatcher,
                    logcatProcessor = logcatUploader,
                    testArtifactsUploader = artifactsUploader
                ),
                logcatProcessor = logcatUploader,
                timeProvider = timeProvider,
                dispatcher = dispatcher
            )
        }

        private fun createTestArtifactsProcessor(
            uploadTestArtifacts: Boolean,
            gson: Gson,
            dispatcher: CoroutineDispatcher,
            logcatProcessor: LogcatProcessor,
            testArtifactsUploader: TestArtifactsUploader
        ): TestArtifactsProcessor {

            return if (uploadTestArtifacts) {
                TestArtifactsProcessorImpl(
                    gson = gson,
                    testArtifactsUploader = testArtifactsUploader,
                    dispatcher = dispatcher,
                    logcatProcessor = logcatProcessor
                )
            } else {
                LegacyTestArtifactsProcessor(
                    gson = gson,
                    logcatProcessor = logcatProcessor,
                    dispatcher = dispatcher
                )
            }
        }
    }
}
