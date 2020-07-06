package com.avito.instrumentation.scheduling

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.report.FlakyTestInfo
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.instrumentation.suite.dex.TestSuiteLoader
import com.avito.instrumentation.suite.dex.check.AllChecks
import com.avito.instrumentation.suite.filter.FilterInfoWriter
import com.avito.report.model.ReportCoordinates
import com.avito.utils.logging.CILogger
import com.google.gson.Gson
import java.io.File

class InstrumentationTestsScheduler(
    private val testsRunner: TestsRunner,
    private val logger: CILogger,
    private val params: InstrumentationTestsAction.Params,
    private val reportCoordinates: ReportCoordinates,
    private val sourceReport: Report,
    private val testSuiteProvider: TestSuiteProvider,
    private val testSuiteLoader: TestSuiteLoader,
    private val gson: Gson,
    private val filterInfoWriter: FilterInfoWriter
) : TestsScheduler {

    override fun schedule(): TestsScheduler.Result {
        filterInfoWriter.writeFilterConfig(params.instrumentationConfiguration.filter)

        val flakyTestInfo = FlakyTestInfo()

        val testSuite = testSuiteProvider.getTestSuite(
            tests = testSuiteLoader.loadTestSuite(params.testApk, AllChecks())
        )

        filterInfoWriter.writeAppliedFilter(testSuite.appliedFilter)
        filterInfoWriter.writeFilterExcludes(testSuite.skippedTests)

        writeTestSuite(testSuite)

        val testsResult = testsRunner.runTests(
            mainApk = params.mainApk,
            testApk = params.testApk,
            runType = TestExecutor.RunType(id = "initialRun"),
            reportCoordinates = reportCoordinates,
            report = sourceReport,
            testsToRun = testSuite.testsToRun
        )

        flakyTestInfo.addReport(testsResult)

        return TestsScheduler.Result(
            testSuite = testSuite,
            testsResult = testsResult,
            flakyInfo = flakyTestInfo.getInfo()
        )
    }

    private fun writeTestSuite(testSuite: TestSuiteProvider.TestSuite) {
        File(params.outputDir, "test-suite.json")
            .writeText(gson.toJson(testSuite.testsToRun.map { it.test }))
    }
}
