package com.avito.instrumentation.scheduling

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.report.FlakyTestInfo
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.instrumentation.suite.dex.TestSuiteLoader
import com.avito.instrumentation.suite.dex.check.AllChecks
import com.avito.report.model.ReportCoordinates

internal class PerformanceTestsScheduler(
    private val testsRunner: TestsRunner,
    private val params: InstrumentationTestsAction.Params,
    private val reportCoordinates: ReportCoordinates,
    private val sourceReport: Report,
    private val testSuiteProvider: TestSuiteProvider,
    private val testSuiteLoader: TestSuiteLoader
) : TestsScheduler {

    override fun schedule(): TestsScheduler.Result {

        val flakyTestInfo = FlakyTestInfo()

        val initialTestSuite = testSuiteProvider.getInitialTestSuite(
            tests = testSuiteLoader.loadTestSuite(
                file = params.testApk,
                testSignatureCheck = AllChecks()
            )
        )

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
}
