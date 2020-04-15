package com.avito.instrumentation.scheduling

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.report.FakeReport
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.rerun.BuildOnTargetCommitForTest
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.instrumentation.suite.dex.FakeTestSuiteLoader
import com.avito.instrumentation.suite.filter.FilterFactory
import com.avito.report.FakeReportsApi
import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertWithMessage
import org.funktionale.tries.Try
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class PerformanceTestsSchedulerTest {

    private lateinit var outputDir: File
    private lateinit var inputDir: File
    private lateinit var sourceApk: File
    private lateinit var sourceTestApk: File
    private lateinit var targetApk: File
    private lateinit var targetTestApk: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        outputDir = File(tempDir, "output").apply { mkdirs() }
        inputDir = File(tempDir, "input").apply { mkdirs() }

        targetApk = File(inputDir, "targetApk").apply { writeText("targetApk") }
        targetTestApk = File(inputDir, "targetTestApk").apply { writeText("targetTestApk") }

        sourceApk = File(inputDir, "sourceApk").apply { writeText("sourceApk") }
        sourceTestApk = File(inputDir, "sourceTestApk").apply { writeText("sourceTestApk") }
    }

    @Test
    fun `performance scheduler - schedules run only on source branch - when target apk is not available`() {
        val initialRun = listOf(
            SimpleRunTest.createStubInstance(),
            SimpleRunTest.createStubInstance()
        )
        val runner = FakeTestsRunner(
            results = listOf(Try.Success(initialRun))
        )
        val scheduler = createPerformanceTestsScheduler(
            runner = runner,
            params = InstrumentationTestsAction.Params.createStubInstance(
                mainApk = sourceApk,
                testApk = sourceTestApk,
                apkOnTargetCommit = targetApk,
                testApkOnTargetCommit = targetTestApk
            )
        )

        val result = scheduler.schedule(
            buildOnTargetCommitResult = BuildOnTargetCommitForTest.Result.ApksUnavailable
        )

        assertWithMessage("initialTestsResult is values from tests runner")
            .that(result.initialTestsResult)
            .isEqualTo(Try.Success(initialRun))
        assertWithMessage("TestsRunner has called only once (for initial run)")
            .that(runner.runTestsRequests)
            .hasSize(1)
        assertWithMessage("Initial tests run called with source applications")
            .that(runner.runTestsRequests.filter { it.mainApk == sourceApk && it.testApk == sourceTestApk })
            .isNotEmpty()
    }

    @Test
    fun `performance scheduler - schedules run on source and target branches - when target apk available`() {
        val initialRun = listOf(
            SimpleRunTest.createStubInstance(),
            SimpleRunTest.createStubInstance()
        )
        val targetRun = listOf(
            SimpleRunTest.createStubInstance(),
            SimpleRunTest.createStubInstance()
        )
        val runner = FakeTestsRunner(
            results = listOf(Try.Success(initialRun), Try.Success(targetRun))
        )
        val scheduler = createPerformanceTestsScheduler(
            runner = runner,
            params = InstrumentationTestsAction.Params.createStubInstance(
                mainApk = sourceApk,
                testApk = sourceTestApk,
                apkOnTargetCommit = targetApk,
                testApkOnTargetCommit = targetTestApk
            )
        )

        val result = scheduler.schedule(
            buildOnTargetCommitResult = BuildOnTargetCommitForTest.Result.OK(
                mainApk = targetApk,
                testApk = targetTestApk
            )
        )

        assertWithMessage("initialTestsResult is values from tests runner")
            .that(result.initialTestsResult)
            .isEqualTo(Try.Success(initialRun))
        assertWithMessage("TestsRunner has called twice (for initial and target runs)")
            .that(runner.runTestsRequests)
            .hasSize(2)
        assertWithMessage("Initial tests run called with source applications")
            .that(runner.runTestsRequests.filter { it.mainApk == sourceApk && it.testApk == sourceTestApk })
            .isNotEmpty()
        assertWithMessage("Rerun on target branch called with target applications")
            .that(runner.runTestsRequests.filter { it.mainApk == targetApk && it.testApk == targetTestApk })
            .isNotEmpty()
    }

    private fun createPerformanceTestsScheduler(
        runner: TestsRunner,
        params: InstrumentationTestsAction.Params = InstrumentationTestsAction.Params.createStubInstance(),
        reportsApi: ReportsApi = FakeReportsApi(),
        sourceReport: Report = FakeReport(),
        targetReport: Report = FakeReport(),
        reportCoordinates: ReportCoordinates = ReportCoordinates.createStubInstance(),
        testSuiteProvider: TestSuiteProvider = TestSuiteProvider.Impl(
            report = FakeReport(),
            targets = params.instrumentationConfiguration.targets,
            reportSkippedTests = params.instrumentationConfiguration.reportSkippedTests,
            filterFactory = FilterFactory.create(
                filterData = params.instrumentationConfiguration.filter,
                impactAnalysisResult = params.impactAnalysisResult,
                reportCoordinates = reportCoordinates,
                reportsFetchApi = reportsApi
            )
        ),
        targetCoordinates: ReportCoordinates = ReportCoordinates.createStubInstance()
    ): PerformanceTestsScheduler = PerformanceTestsScheduler(
        testsRunner = runner,
        testSuiteProvider = testSuiteProvider,
        params = params,
        testSuiteLoader = FakeTestSuiteLoader(),
        reportCoordinates = reportCoordinates,
        targetReportCoordinates = targetCoordinates,
        sourceReport = sourceReport,
        targetReport = targetReport
    )
}
