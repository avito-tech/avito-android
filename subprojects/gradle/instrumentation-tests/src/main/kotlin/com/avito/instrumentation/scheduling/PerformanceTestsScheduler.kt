package com.avito.instrumentation.scheduling

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.report.FlakyTestInfo
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.rerun.BuildOnTargetCommitForTest
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.instrumentation.suite.dex.TestSuiteLoader
import com.avito.instrumentation.suite.dex.TestSuiteLoaderImpl
import com.avito.instrumentation.suite.dex.check.AllChecks
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.instrumentation.util.FutureValue
import com.avito.instrumentation.util.GroupedCoroutinesExecution
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try

internal class PerformanceTestsScheduler(
    private val testsRunner: TestsRunner,
    private val params: InstrumentationTestsAction.Params,
    private val reportCoordinates: ReportCoordinates,
    private val sourceReport: Report,
    private val targetReport: Report,
    private val targetReportCoordinates: ReportCoordinates,
    private val testSuiteProvider: TestSuiteProvider,
    private val testSuiteLoader: TestSuiteLoader = TestSuiteLoaderImpl()
) : TestsScheduler {

    override fun schedule(
        initialTestsSuite: List<TestWithTarget>,
        buildOnTargetCommitResult: BuildOnTargetCommitForTest.Result
    ): TestsScheduler.Result {
        val flakyTestInfo = FlakyTestInfo()

        val group = GroupedCoroutinesExecution()

        var testResultsAfterBranchRerunsFuture: FutureValue<Try<List<SimpleRunTest>>>? = null
        val initialTestsResultFuture = group.launch {
            val initialTestsResult = testsRunner.runTests(
                mainApk = params.mainApk,
                testApk = params.testApk,
                runType = TestExecutor.RunType.Run(id = "initialRun"),
                reportCoordinates = reportCoordinates,
                report = sourceReport,
                testsToRun = initialTestsSuite
            )

            initialTestsResult
        }

        // TODO: почему здесь неявно пропускаем отсутствие сборки? fail-fast?
        if (buildOnTargetCommitResult is BuildOnTargetCommitForTest.Result.OK) {
            testResultsAfterBranchRerunsFuture = group.launch {
                val testsToRun: List<TestWithTarget> =
                    testSuiteProvider.getInitialTestSuite(
                        tests = testSuiteLoader.loadTestSuite(buildOnTargetCommitResult.testApk, AllChecks())
                    )

                val testResultsAfterBranchReruns = testsRunner.runTests(
                    mainApk = buildOnTargetCommitResult.mainApk,
                    testApk = buildOnTargetCommitResult.testApk,
                    runType = TestExecutor.RunType.Run(id = "runOnTarget"),
                    reportCoordinates = targetReportCoordinates,
                    report = targetReport,
                    testsToRun = testsToRun
                )

                testResultsAfterBranchReruns
            }
        }

        group.join()

        val initialTestsResult = initialTestsResultFuture.get()
        val testResultsAfterBranchRerunsResult = testResultsAfterBranchRerunsFuture?.get()

        flakyTestInfo.addReport(initialTestsResult)
        testResultsAfterBranchRerunsResult?.apply {
            flakyTestInfo.addReport(this)
        }

        return TestsScheduler.Result(
            initialTestsResult = initialTestsResult,
            testResultsAfterBranchReruns = initialTestsResult,
            flakyInfo = flakyTestInfo.getInfo()
        )
    }
}
