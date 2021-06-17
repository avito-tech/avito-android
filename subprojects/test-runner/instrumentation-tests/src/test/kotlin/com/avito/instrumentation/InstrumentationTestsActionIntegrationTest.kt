package com.avito.instrumentation

import com.avito.android.Result
import com.avito.android.runner.report.StubReportFactory
import com.avito.android.stats.SeriesName
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.logger.StubLoggerFactory
import com.avito.report.StubReportsApi
import com.avito.report.model.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance
import com.avito.runner.config.InstrumentationConfigurationData
import com.avito.runner.config.InstrumentationTestsActionParams
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.config.createStubInstance
import com.avito.runner.finalizer.FinalizerFactoryImpl
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
    private val reportCoordinates = ReportCoordinates.createStubInstance()
    private val buildFailer = StubBuildFailer()
    private val loggerFactory = StubLoggerFactory

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        outputDir = File(tempDir, "output").apply { mkdirs() }
        inputDir = File(tempDir, "input").apply { mkdirs() }
        apk = File(inputDir, "apk").apply { writeText("some") }
    }

    @Test
    fun `0 tests to run and no previous reports - action success`() {
        val configuration = InstrumentationConfigurationData.createStubInstance(
            name = "newUi",
            targets = singletonList(TargetConfigurationData.createStubInstance())
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
        configuration: InstrumentationConfigurationData,
        params: InstrumentationTestsActionParams = params(configuration),
        seriesName: SeriesName = SeriesName.create("test"),
        timeProvider: StubTimeProvider = StubTimeProvider()
    ) = InstrumentationTestsAction(
        params = params,
        loggerFactory = params.loggerFactory,
        scheduler = StubTestScheduler(),
        finalizer = FinalizerFactoryImpl(
            params = params,
            metricsConfig = RunnerMetricsConfig(params.statsDConfig, seriesName),
            reportFactory = StubReportFactory(),
            timeProvider = timeProvider,
            loggerFactory = loggerFactory,
        ).create(),
        buildFailer = buildFailer,
    )

    private fun params(
        instrumentationConfiguration: InstrumentationConfigurationData
    ) = InstrumentationTestsActionParams.createStubInstance(
        instrumentationConfiguration = instrumentationConfiguration,
        loggerFactory = loggerFactory,
        outputDir = outputDir
    )
}
