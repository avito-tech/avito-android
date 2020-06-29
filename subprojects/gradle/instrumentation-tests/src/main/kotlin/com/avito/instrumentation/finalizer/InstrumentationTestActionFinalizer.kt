package com.avito.instrumentation.finalizer

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.TestRunResult
import com.avito.instrumentation.report.FlakyTestReporter
import com.avito.instrumentation.report.HasFailedTestDeterminer
import com.avito.instrumentation.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.report.JUnitReportWriter
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.report.SendStatisticsAction
import com.avito.instrumentation.scheduling.TestsScheduler
import com.avito.report.ReportViewer
import com.avito.utils.BuildFailer
import com.avito.utils.createOrClear
import com.avito.utils.logging.CILogger
import com.google.gson.Gson
import okhttp3.HttpUrl
import java.io.File

interface InstrumentationTestActionFinalizer {

    fun finalize(
        testsExecutionResults: TestsScheduler.Result
    )

    class Impl(
        private val hasFailedTestDeterminer: HasFailedTestDeterminer,
        private val hasNotReportedTestsDeterminer: HasNotReportedTestsDeterminer,
        private val sourceReport: Report,
        private val params: InstrumentationTestsAction.Params,
        private val flakyTestReporter: FlakyTestReporter,
        private val reportViewer: ReportViewer,
        private val jUnitReportWriter: JUnitReportWriter,
        private val buildFailer: BuildFailer,
        private val gson: Gson,
        private val sendStatisticsAction: SendStatisticsAction,
        private val logger: CILogger
    ) : InstrumentationTestActionFinalizer {

        override fun finalize(
            testsExecutionResults: TestsScheduler.Result
        ) {
            val testRunResult = TestRunResult(
                reportedTests = testsExecutionResults.initialTestsResult.getOrElse { emptyList() },
                failed = hasFailedTestDeterminer.determine(
                    runResult = testsExecutionResults.testResultsAfterBranchReruns
                ),
                notReported = hasNotReportedTestsDeterminer.determine(
                    runResult = testsExecutionResults.initialTestsResult,
                    allTests = testsExecutionResults.initialTestSuite.testsToRun.map { it.test }
                )
            )

            if (testRunResult.notReported is HasNotReportedTestsDeterminer.Result.HasNotReportedTests) {
                sourceReport.sendLostTests(lostTests = testRunResult.notReported.lostTests)
            }

            sourceReport.finish(
                isFullTestSuite = params.isFullTestSuite
            )

            if (params.instrumentationConfiguration.reportFlakyTests) {
                flakyTestReporter.reportSummary(
                    info = testsExecutionResults.flakyInfo
                ).onFailure { logger.critical("Can't send flaky test report", it) }
            }

            sendStatistics()

            val reportViewerUrl = reportViewer.generateReportUrl(
                params.reportCoordinates,
                onlyFailures = testRunResult.failed !is HasFailedTestDeterminer.Result.NoFailed
            )

            jUnitReportWriter.write(
                reportCoordinates = params.reportCoordinates,
                testRunResult = testRunResult,
                destination = junitFile(params.outputDir)
            )

            writeReportViewerLinkFile(
                reportViewerUrl,
                reportViewerFile(params.outputDir)
            )

            logger.info("Report url $reportViewerUrl")

            val verdict = testRunResult.verdict
            val verdictFile = File(params.outputDir, "verdict.json")
            verdictFile.writeText(gson.toJson(verdict))

            when (verdict) {
                is TestRunResult.Verdict.Success -> logger.debug(verdict.message)
                is TestRunResult.Verdict.Failure -> buildFailer.failBuild(
                    "Instrumentation task failed. Look at verdict in the file: $verdictFile"
                )
            }
        }

        /**
         * teamcity report tab
         */
        private fun reportViewerFile(outputDir: File): File = File(outputDir, "rv.html")

        private fun writeReportViewerLinkFile(
            reportViewerUrl: HttpUrl,
            reportFile: File
        ) {
            reportFile.createOrClear()
            reportFile.writeText("<script>location=\"${reportViewerUrl}\"</script>")
        }

        /**
         * teamcity XML report processing
         */
        private fun junitFile(outputDir: File): File = File(outputDir, "junit-report.xml")

        private fun sendStatistics() {
            if (params.sendStatistics) {
                sendStatisticsAction.send()
            } else {
                logger.debug("Send statistics disabled")
            }
        }
    }
}
