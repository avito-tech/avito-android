package com.avito.instrumentation.scheduling

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.report.FlakyTestInfo
import com.avito.instrumentation.report.Report
import com.avito.buildontarget.BuildOnTargetCommitForTest
import com.avito.instrumentation.rerun.MergeResultsWithTargetBranchRun
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.instrumentation.suite.dex.TestSuiteLoader
import com.avito.instrumentation.suite.dex.check.AllChecks
import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.utils.logging.CILogger
import org.funktionale.tries.Try

class InstrumentationTestsScheduler(
    private val testsRunner: TestsRunner,
    private val reportsApi: ReportsApi,
    private val logger: CILogger,
    private val params: InstrumentationTestsAction.Params,
    private val reportCoordinates: ReportCoordinates,
    private val targetReportCoordinates: ReportCoordinates,
    private val sourceReport: Report,
    private val targetReport: Report,
    private val testSuiteProvider: TestSuiteProvider,
    private val testSuiteLoader: TestSuiteLoader
) : TestsScheduler {

    override fun schedule(
        buildOnTargetCommitResult: BuildOnTargetCommitForTest.Result
    ): TestsScheduler.Result {
        val flakyTestInfo = FlakyTestInfo()
        val initialTestSuite = testSuiteProvider.getInitialTestSuite(
            tests = testSuiteLoader.loadTestSuite(params.testApk, AllChecks())
        )
        val initialTestsResult = testsRunner.runTests(
            mainApk = params.mainApk,
            testApk = params.testApk,
            runType = TestExecutor.RunType.Run(id = "initialRun"),
            reportCoordinates = reportCoordinates,
            report = sourceReport,
            testsToRun = initialTestSuite.testsToRun
        )

        val (
            finalSourceBranchState: Try<List<SimpleRunTest>>,
            finalTargetBranchState: Try<List<SimpleRunTest>>?
        ) = when {
            !params.instrumentationConfiguration.tryToReRunOnTargetBranch -> {
                logger.info("Rerun on target branch disabled")
                initialTestsResult to null
            }
            initialTestsResult is Try.Failure -> {
                logger.info("Initial run on source branch cancelled: Can't get rerun on target results")
                initialTestsResult to null
            }
            else -> {

                var sourceReportState = initialTestsResult
                val attempts = 2

                repeat(attempts) {
                    // начинаем с 1
                    val attempt = it + 1

                    sourceReportState = rerunOnTargetAndMergeWithSourceBranch(
                        currentReportState = sourceReportState,
                        buildOnTargetCommit = buildOnTargetCommitResult,
                        targetReportCoordinates = targetReportCoordinates,
                        rerunAttempt = attempt,
                        targetReport = targetReport
                    )

                    sourceReportState = rerunOnSource(
                        currentReportState = sourceReportState,
                        rerunAttempt = attempt,
                        report = sourceReport
                    )
                }

                val targetReportState = reportsApi.getTestsForRunId(targetReportCoordinates)

                sourceReportState to targetReportState
            }
        }

        flakyTestInfo.addReport(finalSourceBranchState)
        if (finalTargetBranchState != null) {
            flakyTestInfo.addReport(finalTargetBranchState)
        }

        return TestsScheduler.Result(
            initialTestSuite = initialTestSuite,
            initialTestsResult = initialTestsResult,
            testResultsAfterBranchReruns = finalSourceBranchState,
            flakyInfo = flakyTestInfo.getInfo()
        )
    }

    /**
     * кажется ОК писать все репорты на таргете в один отчет
     */
    private fun rerunOnTargetAndMergeWithSourceBranch(
        currentReportState: Try<List<SimpleRunTest>>,
        buildOnTargetCommit: BuildOnTargetCommitForTest.Result,
        targetReportCoordinates: ReportCoordinates,
        targetReport: Report,
        rerunAttempt: Int
    ): Try<List<SimpleRunTest>> {
        return when {
            currentReportState is Try.Failure -> {
                logger.debug("Rerun on target branch cancelled: Can't get current report state")
                currentReportState
            }
            buildOnTargetCommit is BuildOnTargetCommitForTest.Result.ApksUnavailable -> {
                logger.debug("Rerun on target branch cancelled: target apks unavailable")
                currentReportState
            }
            else -> {
                // cast is not smart enough
                buildOnTargetCommit as BuildOnTargetCommitForTest.Result.OK

                val testsToRunOnTarget = testSuiteProvider.getRerunTestsSuite(
                    testSuiteLoader.loadTestSuite(buildOnTargetCommit.testApk)
                ).testsToRun

                // число перезапусков определяется
                val rerunResults = testsRunner.runTests(
                    mainApk = buildOnTargetCommit.mainApk,
                    testApk = buildOnTargetCommit.testApk,
                    runType = TestExecutor.RunType.Rerun("rerunOnTarget-$rerunAttempt"),
                    reportCoordinates = targetReportCoordinates,
                    report = targetReport,
                    testsToRun = testsToRunOnTarget
                )

                MergeResultsWithTargetBranchRun(
                    reports = reportsApi,
                    logger = logger,
                    mainReportCoordinates = reportCoordinates
                ).merge(
                    initialRunResults = currentReportState,
                    rerunResults = rerunResults
                )
            }
        }
    }

    private fun rerunOnSource(
        currentReportState: Try<List<SimpleRunTest>>,
        rerunAttempt: Int,
        report: Report
    ): Try<List<SimpleRunTest>> = currentReportState.fold(
        {
            val testsToRun = testSuiteProvider.getRerunTestsSuite(
                testSuiteLoader.loadTestSuite(params.testApk)
            ).testsToRun

            // сознательно берем число перезапусков с первого запуска
            testsRunner.runTests(
                mainApk = params.mainApk,
                testApk = params.testApk,
                runType = TestExecutor.RunType.Run("rerunOnSource-$rerunAttempt"),
                //пишем в главный отчет
                reportCoordinates = reportCoordinates,
                report = report,
                testsToRun = testsToRun
            )

            // здесь ничего не мерджим, работает как обычный перезапуск по квоте
        },
        {
            logger.debug("Rerun on source branch cancelled: Can't get rerun on target results")
            currentReportState
        }
    )
}
