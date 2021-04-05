package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.internal.InstrumentationTestsAction
import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.report.HasFailedTestDeterminer
import com.avito.instrumentation.internal.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.ReportViewer
import com.avito.utils.BuildFailer
import com.google.gson.Gson
import java.io.File

internal interface InstrumentationTestActionFinalizer {

    fun finalize(
        testsExecutionResults: TestsScheduler.Result
    )

    interface FinalizeAction {

        fun action(testRunResult: TestRunResult)
    }

    class Impl(
        private val hasFailedTestDeterminer: HasFailedTestDeterminer,
        private val hasNotReportedTestsDeterminer: HasNotReportedTestsDeterminer,
        private val params: InstrumentationTestsAction.Params,
        private val reportViewer: ReportViewer,
        private val buildFailer: BuildFailer,
        private val gson: Gson,
        private val actions: List<FinalizeAction>,
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
            actions.forEach { it.action(testRunResult) }

            val verdict = testRunResult.verdict
            val verdictFile = writeVerdict(testRunResult)

            logger.debug("Test run verdict: \n\t$verdict")

            when (verdict) {
                is TestRunResult.Verdict.Success -> {
                    // empty
                }
                is TestRunResult.Verdict.Failure -> buildFailer.failBuild(
                    "Instrumentation task failed. Look at verdict in the file: $verdictFile"
                )
            }
        }

        private fun writeVerdict(testRunResult: TestRunResult): File {
            val reportViewerUrl = reportViewer.generateReportUrl(params.reportCoordinates)

            val verdictFile = params.verdictFile
            verdictFile.writeText(
                gson.toJson(
                    InstrumentationTestsTask.Verdict(
                        reportUrl = reportViewerUrl.toString(),
                        testRunVerdict = testRunResult.verdict
                    )
                )
            )
            return verdictFile
        }
    }
}
