package com.avito.instrumentation

import com.avito.android.Result
import com.avito.android.StubTestSuiteLoader
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.android.runner.devices.StubDeviceProviderFactory
import com.avito.android.runner.report.StubReport
import com.avito.android.runner.report.StubReportFactory
import com.avito.android.stats.SeriesName
import com.avito.http.HttpClientProvider
import com.avito.http.createStubInstance
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.InstrumentationTestsActionFactory
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.executing.TestExecutor
import com.avito.instrumentation.internal.executing.TestExecutorFactory
import com.avito.instrumentation.internal.finalizer.FinalizerFactory
import com.avito.instrumentation.internal.scheduling.TestsSchedulerFactory
import com.avito.instrumentation.stub.createStubInstance
import com.avito.instrumentation.stub.executing.StubTestExecutor
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.report.StubReportsApi
import com.avito.report.model.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance
import com.avito.runner.scheduler.listener.TestLifecycleListener
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.StubTimeProvider
import com.avito.utils.StubBuildFailer
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Collections.singletonList

internal class InstrumentationTestsActionIntegrationTest {

    private lateinit var inputDir: File
    private lateinit var apk: File
    private lateinit var outputDir: File
    private val reportsApi = StubReportsApi()
    private val testSuiteLoader = StubTestSuiteLoader()
    private val reportCoordinates = ReportCoordinates.createStubInstance()
    private val testRunner = StubTestExecutor()
    private val testExecutorFactory = object : TestExecutorFactory {
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
            return testRunner
        }
    }
    private val buildFailer = StubBuildFailer()
    private val loggerFactory = StubLoggerFactory

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        outputDir = File(tempDir, "output").apply { mkdirs() }
        inputDir = File(tempDir, "input").apply { mkdirs() }
        apk = File(inputDir, "apk").apply { writeText("some") }
    }

    @Test
    fun `action - ok - 0 tests to run, no previous reports`() {
        val configuration = InstrumentationConfiguration.Data.createStubInstance(
            name = "newUi",
            targets = singletonList(TargetConfiguration.Data.createStubInstance())
        )
        reportsApi.getReportResult = Result.Success(
            Report(
                id = "stub",
                planSlug = "planSlug",
                jobSlug = "jobSlug",
                runId = "runId",
                isFinished = false,
                buildBranch = "buildBranch"
            )
        )
        reportsApi.enqueueTestsForRunId(reportCoordinates, Result.Success(emptyList()))

        createAction(configuration = configuration).run()

        assertThat(buildFailer.lastReason).isNull()
    }

    private fun createAction(
        configuration: InstrumentationConfiguration.Data,
        params: InstrumentationTestsAction.Params = params(configuration),
        seriesName: SeriesName = SeriesName.create("test"),
        reportFactory: StubReportFactory = StubReportFactory()
    ) = InstrumentationTestsAction(
        params = params,
        loggerFactory = params.loggerFactory,
        scheduler = TestsSchedulerFactory.Impl(
            params = params,
            gson = InstrumentationTestsActionFactory.gson,
            metricsConfig = RunnerMetricsConfig(params.statsDConfig, SeriesName.create("runner")),
            testExecutorFactory = testExecutorFactory,
            testSuiteLoader = testSuiteLoader,
            timeProvider = StubTimeProvider(),
            httpClientProvider = HttpClientProvider.createStubInstance(),
            report = StubReport(),
            reportFactory = reportFactory
        ).create(devicesProviderFactory = StubDeviceProviderFactory),
        finalizer = FinalizerFactory.Impl(
            params = params,
            metricsConfig = RunnerMetricsConfig(params.statsDConfig, seriesName),
            reportFactory = StubReportFactory(),
            buildFailer = buildFailer,
            gson = InstrumentationTestsActionFactory.gson
        ).create()
    )

    private fun params(
        instrumentationConfiguration: InstrumentationConfiguration.Data
    ) = InstrumentationTestsAction.Params.createStubInstance(
        instrumentationConfiguration = instrumentationConfiguration,
        loggerFactory = loggerFactory,
        outputDir = outputDir
    )
}
