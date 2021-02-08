package com.avito.instrumentation

import com.avito.android.StubTestSuiteLoader
import com.avito.android.TestInApk
import com.avito.android.createStubInstance
import com.avito.android.runner.devices.DevicesProviderFactory
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.executing.ExecutionParameters
import com.avito.instrumentation.internal.executing.TestExecutor
import com.avito.instrumentation.internal.executing.TestExecutorFactory
import com.avito.instrumentation.internal.finalizer.FinalizerFactory
import com.avito.instrumentation.internal.report.listener.TestReporter
import com.avito.instrumentation.internal.scheduling.TestsSchedulerFactory
import com.avito.instrumentation.stub.createStubInstance
import com.avito.instrumentation.stub.executing.StubTestExecutor
import com.avito.instrumentation.stub.report.createStubInstance
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.report.StubReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.CreateResult
import com.avito.report.model.GetReportResult
import com.avito.report.model.Report
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.createStubInstance
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.utils.StubBuildFailer
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.funktionale.tries.Try
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
            testReporter: TestReporter,
            buildId: String,
            configuration: InstrumentationConfiguration.Data,
            executionParameters: ExecutionParameters,
            loggerFactory: LoggerFactory,
            metricsConfig: RunnerMetricsConfig
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
        reportsApi.getReportResult = GetReportResult.Found(
            Report(
                id = "stub",
                planSlug = "planSlug",
                jobSlug = "jobSlug",
                runId = "runId",
                isFinished = false,
                buildBranch = "buildBranch"
            )
        )
        reportsApi.enqueueTestsForRunId(reportCoordinates, Try.Success(emptyList()))

        createAction(configuration = configuration).run()

        assertThat(buildFailer.lastReason).isNull()
    }

    @Test
    fun `inconsistent test run - missing tests reported as lost and failed build`() {
        val configuration = InstrumentationConfiguration.Data.createStubInstance(
            targets = listOf(
                TargetConfiguration.Data.createStubInstance(deviceName = "api22")
            )
        )

        val reportId = "1234"
        reportsApi.createResult = CreateResult.Created(reportId)
        reportsApi.getReportResult = GetReportResult.Found(
            Report(
                id = reportId,
                planSlug = "planSlug",
                jobSlug = "jobSlug",
                runId = "runId",
                isFinished = false,
                buildBranch = "buildBranch"
            )
        )

        testSuiteLoader.result.addAll(
            listOf(
                TestInApk.createStubInstance(className = "com.Test", methodName = "test1"),
                TestInApk.createStubInstance(className = "com.Test", methodName = "test2"),
                TestInApk.createStubInstance(className = "com.Test", methodName = "test3")
            )
        )

        reportsApi.enqueueTestsForRunId(
            reportCoordinates = reportCoordinates,
            value = Try.Success(
                listOf(
                    SimpleRunTest.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                    SimpleRunTest.createStubInstance(
                        name = "com.Test.test2",
                        deviceName = "anotherApi"
                    ),
                    SimpleRunTest.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                )
            )
        )

        reportsApi.finished = Try.Success(Unit)

        createAction(configuration).run()

        assertThat(reportsApi.addTestsRequests.last().tests).containsExactly(
            AndroidTest.Lost.createStubInstance(
                name = "com.Test.test2",
                deviceName = "api22"
            )
        )

        assertWithMessage("inconsistent test run should fail build").that(buildFailer.lastReason)
            .isNotNull()
    }

    private fun createAction(
        configuration: InstrumentationConfiguration.Data,
        params: InstrumentationTestsAction.Params = params(configuration)
    ) = InstrumentationTestsAction(
        params = params,
        loggerFactory = params.loggerFactory,
        scheduler = TestsSchedulerFactory.Impl(
            params = params,
            sourceReport = com.avito.instrumentation.report.Report.createStubInstance(
                reportsApi = reportsApi,
                reportCoordinates = reportCoordinates,
                buildId = params.buildId,
            ),
            testExecutorFactory = testExecutorFactory,
            testSuiteLoader = testSuiteLoader
        ).create(),
        finalizer = FinalizerFactory.Impl(
            params = params,
            sourceReport = com.avito.instrumentation.report.Report.createStubInstance(
                reportsApi = reportsApi,
                reportCoordinates = reportCoordinates,
                buildId = params.buildId
            ),
            buildFailer = buildFailer
        ).create()
    )

    private fun params(
        instrumentationConfiguration: InstrumentationConfiguration.Data
    ) = InstrumentationTestsAction.Params.createStubInstance(
        instrumentationConfiguration = instrumentationConfiguration,
        loggerFactory = loggerFactory,
        outputDir = outputDir,
        reportCoordinates = reportCoordinates
    )
}
