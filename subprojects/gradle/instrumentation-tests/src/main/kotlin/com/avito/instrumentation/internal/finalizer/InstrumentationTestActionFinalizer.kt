package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.report.HasFailedTestDeterminer
import com.avito.instrumentation.internal.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.report.JUnitReportWriter
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.instrumentation.metrics.InstrumentationMetricsSender
import com.avito.instrumentation.report.Report
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.ReportViewer
import com.avito.utils.BuildFailer
import com.avito.utils.createOrClear
import com.google.gson.Gson
import okhttp3.HttpUrl
import java.io.File

internal interface InstrumentationTestActionFinalizer {

    fun finalize(
        testsExecutionResults: TestsScheduler.Result
    )

    class Impl(
        private val hasFailedTestDeterminer: HasFailedTestDeterminer,
        private val hasNotReportedTestsDeterminer: HasNotReportedTestsDeterminer,
        private val sourceReport: Report,
        private val params: InstrumentationTestsAction.Params,
        private val reportViewer: ReportViewer,
        private val jUnitReportWriter: JUnitReportWriter,
        private val buildFailer: BuildFailer,
        private val metricsSender: InstrumentationMetricsSender,
        private val gson: Gson,
        loggerFactory: LoggerFactory
    ) : InstrumentationTestActionFinalizer {

        private val logger = loggerFactory.create<InstrumentationTestActionFinalizer>()

        override fun finalize(
            testsExecutionResults: TestsScheduler.Result
        ) {
            val testRunResult = TestRunResult(
                reportedTests = testsExecutionResults.testsResult.getOrElse { emptyList() },
                failed = hasFailedTestDeterminer.determine(
                    runResult = testsExecutionResults.testsResult
                ),
                notReported = hasNotReportedTestsDeterminer.determine(
                    runResult = testsExecutionResults.testsResult,
                    allTests = testsExecutionResults.testSuite.testsToRun.map { it.test }
                )
            )

            if (testRunResult.notReported is HasNotReportedTestsDeterminer.Result.HasNotReportedTests) {
                val lostTests = testRunResult.notReported.lostTests
                sourceReport.sendLostTests(lostTests)
                metricsSender.sendNotReportedCount(lostTests.size)
            }

            sourceReport.finish()

            jUnitReportWriter.write(
                reportCoordinates = params.reportCoordinates,
                testRunResult = testRunResult,
                destination = junitFile(params.outputDir)
            )

            val verdict = testRunResult.verdict
            val reportViewerUrl = reportViewer.generateReportUrl(
                params.reportCoordinates,
                onlyFailures = verdict is TestRunResult.Verdict.Failure
            )

            writeReportViewerLinkFile(
                reportViewerUrl,
                reportViewerFile(params.outputDir)
            )

            val verdictFile = params.verdictFile
            verdictFile.writeText(
                gson.toJson(
                    InstrumentationTestsTask.Verdict(
                        reportUrl = reportViewerUrl.toString(),
                        testRunVerdict = verdict
                    )
                )
            )

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
    }
}
