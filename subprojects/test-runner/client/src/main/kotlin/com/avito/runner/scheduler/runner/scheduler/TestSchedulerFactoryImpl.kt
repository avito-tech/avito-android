package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.TestSuiteLoader
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.report.Report
import com.avito.android.runner.report.ReportFactory
import com.avito.android.stats.StatsDSender
import com.avito.filestorage.RemoteStorageFactory
import com.avito.http.HttpClientProvider
import com.avito.report.model.TestStaticData
import com.avito.report.serialize.ReportSerializer
import com.avito.retrace.ProguardRetracer
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.scheduler.listener.AvitoFileStorageUploader
import com.avito.runner.scheduler.listener.LegacyTestArtifactsProcessor
import com.avito.runner.scheduler.listener.LogcatProcessor
import com.avito.runner.scheduler.listener.LogcatTestLifecycleListener
import com.avito.runner.scheduler.listener.ReportProcessor
import com.avito.runner.scheduler.listener.ReportProcessorImpl
import com.avito.runner.scheduler.listener.TestArtifactsProcessor
import com.avito.runner.scheduler.listener.TestArtifactsProcessorImpl
import com.avito.runner.scheduler.listener.TestArtifactsUploader
import com.avito.runner.scheduler.metrics.InstrumentationMetricsSender
import com.avito.runner.scheduler.runner.TestExecutorFactory
import com.avito.runner.scheduler.suite.TestSuiteProvider
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.FilterInfoWriter
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import java.nio.file.Files

public class TestSchedulerFactoryImpl(
    private val params: InstrumentationTestsActionParams,
    private val report: Report,
    private val timeProvider: TimeProvider,
    private val httpClientProvider: HttpClientProvider,
    private val metricsConfig: RunnerMetricsConfig,
    private val testExecutorFactory: TestExecutorFactory,
    private val testSuiteLoader: TestSuiteLoader,
    private val reportFactory: ReportFactory
) : TestsSchedulerFactory {

    override fun create(devicesProviderFactory: DevicesProviderFactory): TestsScheduler {
        val tempDir = Files.createTempDirectory(null).toFile()
        val testRunner: TestsRunner = createTestRunner(devicesProviderFactory, tempDir)
        val testSuiteProvider: TestSuiteProvider = createTestSuiteProvider()

        return InstrumentationTestsScheduler(
            testsRunner = testRunner,
            params = params,
            report = report,
            testSuiteProvider = testSuiteProvider,
            testSuiteLoader = testSuiteLoader,
            filterInfoWriter = FilterInfoWriter.Impl(
                outputDir = params.outputDir,
            ),
            loggerFactory = params.loggerFactory
        )
    }

    private fun createTestSuiteProvider(): TestSuiteProvider = TestSuiteProvider.Impl(
        report = report,
        targets = params.instrumentationConfiguration.targets,
        reportSkippedTests = params.instrumentationConfiguration.reportSkippedTests,
        filterFactory = FilterFactory.create(
            filterData = params.instrumentationConfiguration.filter,
            impactAnalysisResult = params.impactAnalysisResult,
            reportFactory = reportFactory,
            loggerFactory = params.loggerFactory
        )
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
            projectName = params.projectName,
            saveTestArtifactsToOutputs = params.saveTestArtifactsToOutputs,
            fetchLogcatForIncompleteTests = params.fetchLogcatForIncompleteTests,
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
                endpoint = params.fileStorageUrl.toHttpUrl(),
                httpClientProvider = httpClientProvider,
                isAndroidRuntime = false
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
                reportSerializer = ReportSerializer(),
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
        reportSerializer: ReportSerializer,
        dispatcher: CoroutineDispatcher,
        logcatProcessor: LogcatProcessor,
        testArtifactsUploader: TestArtifactsUploader
    ): TestArtifactsProcessor {

        return if (uploadTestArtifacts) {
            TestArtifactsProcessorImpl(
                reportSerializer = reportSerializer,
                testArtifactsUploader = testArtifactsUploader,
                dispatcher = dispatcher,
                logcatProcessor = logcatProcessor
            )
        } else {
            LegacyTestArtifactsProcessor(
                reportSerializer = reportSerializer,
                logcatProcessor = logcatProcessor,
                dispatcher = dispatcher
            )
        }
    }
}
