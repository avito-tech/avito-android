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

        val initialTestSuite = testSuiteProvider.getInitialTestSuite(
            tests = testSuiteLoader.loadTestSuite(params.testApk, AllChecks())
        )

        filterInfoWriter.writeAppliedFilter(initialTestSuite.appliedFilter)
        filterInfoWriter.writeFilterExcludes(initialTestSuite.skippedTests)

        writeInitialTestSuite(initialTestSuite)

        val initialTestsResult = testsRunner.runTests(
            mainApk = params.mainApk,
            testApk = params.testApk,
            runType = TestExecutor.RunType.Run(id = "initialRun"),
            reportCoordinates = reportCoordinates,
            report = sourceReport,
            testsToRun = initialTestSuite.testsToRun
        )

        flakyTestInfo.addReport(initialTestsResult)

        return TestsScheduler.Result(
            initialTestSuite = initialTestSuite,
            initialTestsResult = initialTestsResult,
            testResultsAfterBranchReruns = initialTestsResult,
            flakyInfo = flakyTestInfo.getInfo()
        )
    }

    private fun writeInitialTestSuite(initialTestSuite: TestSuiteProvider.TestSuite) {
        File(params.outputDir, "initial-suite.json").writeText(gson.toJson(initialTestSuite.testsToRun.map { it.test }))
    }
}
