package com.avito.instrumentation.scheduling

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.report.FakeReport
import com.avito.instrumentation.report.ReadReport
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.instrumentation.suite.dex.FakeTestSuiteLoader
import com.avito.instrumentation.suite.filter.FilterFactory
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

        val result = scheduler.schedule()

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

    private fun createPerformanceTestsScheduler(
        runner: TestsRunner,
        params: InstrumentationTestsAction.Params = InstrumentationTestsAction.Params.createStubInstance(),
        reportCoordinates: ReportCoordinates = ReportCoordinates.createStubInstance(),
        testSuiteProvider: TestSuiteProvider = TestSuiteProvider.Impl(
            report = FakeReport(),
            targets = params.instrumentationConfiguration.targets,
            reportSkippedTests = params.instrumentationConfiguration.reportSkippedTests,
            filterFactory = FilterFactory.create(
                filterData = params.instrumentationConfiguration.filter,
                impactAnalysisResult = params.impactAnalysisResult,
                reportConfig = Report.Factory.Config.ReportViewerCoordinates(
                    ReportCoordinates.createStubInstance(),
                    "stub"
                ),
                factory = object : Report.Factory {
                    override fun createReport(config: Report.Factory.Config): Report {
                        return FakeReport()
                    }

                    override fun createReadReport(config: Report.Factory.Config): ReadReport {
                        return FakeReport()
                    }

                }
            )
        )
    ): PerformanceTestsScheduler = PerformanceTestsScheduler(
        testsRunner = runner,
        testSuiteProvider = testSuiteProvider,
        params = params,
        testSuiteLoader = FakeTestSuiteLoader(),
        reportCoordinates = reportCoordinates,
        sourceReport = FakeReport()
    )
}
