package com.avito.instrumentation.scheduling

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.executing.TestExecutor
import com.avito.instrumentation.report.FlakyTestInfo
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.rerun.BuildOnTargetCommitForTest
import com.avito.instrumentation.rerun.MergeResultsWithTargetBranchRun
import com.avito.instrumentation.suite.TestSuiteProvider
import com.avito.instrumentation.suite.model.TestWithTarget
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
    private val testSuiteProvider: TestSuiteProvider
) : TestsScheduler {

    /**
     * Тут мы сейчас всегда получим отчет, даже если прогона не было, т.к. создаем его для CD предварительно
     */
    private val previousRun by lazy { reportsApi.getTestsForRunId(reportCoordinates) }

    override fun schedule(
        initialTestsSuite: List<TestWithTarget>,
        buildOnTargetCommit: BuildOnTargetCommitForTest.RunOnTargetCommitResolution
    ): TestsScheduler.Result {
        val flakyTestInfo = FlakyTestInfo()

        val initialTestsResult = testsRunner.runTests(
            mainApk = params.mainApk,
            testApk = params.testApk,
            runType = TestExecutor.RunType.Run(id = "initialRun"),
            reportCoordinates = reportCoordinates,
            testsToRun = initialTestsSuite,
            currentReportState = { previousRun },
            report = sourceReport
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
                logger.info("Rerun on source branch cancelled: Can't get rerun on target results")
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
                        buildOnTargetCommit = buildOnTargetCommit,
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
        buildOnTargetCommit: BuildOnTargetCommitForTest.RunOnTargetCommitResolution,
        targetReportCoordinates: ReportCoordinates,
        targetReport: Report,
        rerunAttempt: Int
    ): Try<List<SimpleRunTest>> {
        return when {
            currentReportState is Try.Failure -> {
                logger.info("Rerun on target branch cancelled: Can't get current report state")
                currentReportState
            }
            buildOnTargetCommit is BuildOnTargetCommitForTest.RunOnTargetCommitResolution.ApksUnavailable -> {
                logger.info("Rerun on target branch cancelled: target apks unavailable")
                currentReportState
            }
            else -> {
                // cast is not smart enough
                buildOnTargetCommit as BuildOnTargetCommitForTest.RunOnTargetCommitResolution.OK
                val currentState = currentReportState.get()

                val testsToRunOnTarget: List<TestWithTarget> = testSuiteProvider.getFailedOnlySuite(
                    testApk = buildOnTargetCommit.testApk,
                    params = params,
                    previousRun = { currentState }
                )

                // число перезапусков определяется
                val rerunResults = testsRunner.runTests(
                    mainApk = buildOnTargetCommit.mainApk,
                    testApk = buildOnTargetCommit.testApk,
                    runType = TestExecutor.RunType.Rerun("rerunOnTarget-$rerunAttempt"),
                    reportCoordinates = targetReportCoordinates,
                    testsToRun = testsToRunOnTarget,
                    currentReportState = { currentReportState },
                    report = targetReport
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
        { currentState ->
            val testsToRun: List<TestWithTarget> = testSuiteProvider.getFailedOnlySuite(
                testApk = params.testApk,
                params = params,
                previousRun = { currentState }
            )

            // сознательно берем число перезапусков с первого запуска
            testsRunner.runTests(
                mainApk = params.mainApk,
                testApk = params.testApk,
                runType = TestExecutor.RunType.Run("rerunOnSource-$rerunAttempt"),
                //пишем в главный отчет
                reportCoordinates = reportCoordinates,
                testsToRun = testsToRun,
                currentReportState = { currentReportState },
                report = report
            )

            // здесь ничего не мерджим, работает как обычный перезапуск по квоте
        },
        {
            logger.info("Rerun on source branch cancelled: Can't get rerun on target results")
            currentReportState
        }
    )
}
