package com.avito.runner.scheduler

import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.report.Report
import com.avito.filestorage.RemoteStorageFactory
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.report.model.TestStaticData
import com.avito.report.serialize.ReportSerializer
import com.avito.retrace.ProguardRetracer
import com.avito.runner.config.RunnerInputParams
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.reservation.DeviceReservationWatcher
import com.avito.runner.scheduler.listener.ArtifactsTestListener
import com.avito.runner.scheduler.listener.AvitoFileStorageUploader
import com.avito.runner.scheduler.listener.LegacyTestArtifactsProcessor
import com.avito.runner.scheduler.listener.LogListener
import com.avito.runner.scheduler.listener.LogcatProcessor
import com.avito.runner.scheduler.listener.LogcatTestLifecycleListener
import com.avito.runner.scheduler.listener.ReportProcessor
import com.avito.runner.scheduler.listener.ReportProcessorImpl
import com.avito.runner.scheduler.listener.TestArtifactsProcessor
import com.avito.runner.scheduler.listener.TestArtifactsProcessorImpl
import com.avito.runner.scheduler.listener.TestArtifactsUploader
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.scheduler.metrics.InstrumentationMetricsSender
import com.avito.runner.scheduler.metrics.TestMetricsListener
import com.avito.runner.scheduler.report.CompositeReporter
import com.avito.runner.scheduler.report.SummaryReportMakerImpl
import com.avito.runner.scheduler.report.trace.TraceReporter
import com.avito.runner.scheduler.runner.TestRunner
import com.avito.runner.scheduler.runner.TestRunnerExecutionState
import com.avito.runner.scheduler.runner.TestRunnerImpl
import com.avito.runner.scheduler.runner.model.TestRunRequestFactory
import com.avito.runner.scheduler.runner.scheduler.TestExecutionScheduler
import com.avito.runner.service.listener.CompositeListener
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.TestCase
import com.avito.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File

internal class TestRunnerFactoryImpl(
    private val testRunnerOutputDir: File,
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory,
    private val testMetricsListener: TestMetricsListener,
    private val devicesProvider: DevicesProvider,
    private val testRunnerRequestFactory: TestRunRequestFactory,
    private val executionState: TestRunnerExecutionState,
    private val httpClientProvider: HttpClientProvider,
    private val params: RunnerInputParams,
    private val tempLogcatDir: File,
    private val metricsSender: InstrumentationMetricsSender,
    private val report: Report,
    private val targets: List<TargetConfigurationData>
) : TestRunnerFactory {

    override fun createTestRunner(
        tests: List<TestStaticData>
    ): TestRunner {
        return TestRunnerImpl(
            scheduler = TestExecutionScheduler(
                results = executionState.results,
                intentions = executionState.intentions,
                intentionResults = executionState.intentionResults
            ),
            devicesProvider = devicesProvider,
            reservationWatcher = DeviceReservationWatcher.Impl(
                reservation = devicesProvider
            ),
            loggerFactory = loggerFactory,
            state = executionState,
            summaryReportMaker = SummaryReportMakerImpl(),
            reporter = CompositeReporter(
                reporters = setOf(
                    TraceReporter(
                        runName = "Tests",
                        outputDirectory = testRunnerOutputDir
                    )
                )
            ),
            testMetricsListener = testMetricsListener,
            testRunRequestFactory = testRunnerRequestFactory,
            testListener = CompositeListener(
                listeners = mutableListOf<TestListener>().apply {
                    add(LogListener())
                    add(
                        ArtifactsTestListener(
                            lifecycleListener = createTestReporter(
                                testStaticDataByTestCase = testStaticDataByTestCase(tests),
                            ),
                            outputDirectory = testRunnerOutputDir,
                            loggerFactory = loggerFactory,
                            saveTestArtifactsToOutputs = params.saveTestArtifactsToOutputs,
                            fetchLogcatForIncompleteTests = params.fetchLogcatForIncompleteTests,
                        )
                    )
                }
            ),
            targets = targets
        )
    }

    private fun testStaticDataByTestCase(
        testsToRun: List<TestStaticData>
    ): Map<TestCase, TestStaticData> {
        return testsToRun.associateBy { test ->
            TestCase(
                name = test.name,
                deviceName = test.device
            )
        }
    }

    private fun createTestReporter(
        testStaticDataByTestCase: Map<TestCase, TestStaticData>,
    ): TestLifecycleListener {
        return LogcatTestLifecycleListener(
            logcatDir = tempLogcatDir,
            reportProcessor = createReportProcessor(testStaticDataByTestCase, metricsSender),
            report = report,
        )
    }

    private fun createReportProcessor(
        testSuite: Map<TestCase, TestStaticData>,
        metricsSender: InstrumentationMetricsSender
    ): ReportProcessor {

        val dispatcher = Dispatchers.IO

        val reTracer: ProguardRetracer = ProguardRetracer.Impl(params.proguardMappings)

        val artifactsUploader: TestArtifactsUploader = AvitoFileStorageUploader(
            RemoteStorageFactory.create(
                endpoint = params.fileStorageUrl.toHttpUrl(),
                httpClientProvider = httpClientProvider,
                isAndroidRuntime = false
            )
        )

        val logcatUploader = LogcatProcessor.Impl(
            testArtifactsUploader = artifactsUploader,
            retracer = reTracer
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
