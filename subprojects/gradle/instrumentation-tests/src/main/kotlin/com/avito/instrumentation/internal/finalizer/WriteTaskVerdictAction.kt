package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer.FinalizeAction
import com.avito.instrumentation.internal.verdict.InstrumentationTestsTaskVerdict
import com.avito.report.ReportViewer
import com.avito.report.model.ReportCoordinates
import com.google.gson.Gson
import java.io.File

internal class WriteTaskVerdictAction(
    private val coordinates: ReportCoordinates,
    private val verdictDestination: File,
    private val reportViewer: ReportViewer,
    private val gson: Gson
) : FinalizeAction {

    override fun action(testRunResult: TestRunResult) {
        val reportViewerUrl = reportViewer.generateReportUrl(coordinates)
        verdictDestination.writeText(
            gson.toJson(
                InstrumentationTestsTaskVerdict(
                    title = testRunResult.verdict.message,
                    reportUrl = reportViewerUrl.toString(),
                    causeFailureTests = testRunResult.verdict.getCauseFailureTests()
                )
            )
        )
    }

    private fun TestRunResult.Verdict.getCauseFailureTests() =
        when (this) {
            is TestRunResult.Verdict.Success -> emptySet()
            is TestRunResult.Verdict.Failure -> {
                val failedTestsVerdict = prettifiedDetails.failedTests
                    .map { test -> test.toTaskVerdictTest("FAILED") }

                val lostTestsVerdict = prettifiedDetails.lostTests
                    .map { test -> test.toTaskVerdictTest("LOST") }
                (failedTestsVerdict + lostTestsVerdict).toSet()
            }
        }

    private fun TestRunResult.Verdict.Failure.Details.Test.toTaskVerdictTest(
        prefix: String
    ): InstrumentationTestsTaskVerdict.Test = InstrumentationTestsTaskVerdict.Test(
        testUrl = reportViewer.generateSingleTestRunUrl(
            coordinates,
            name.className,
            name.methodName
        ).toString(),
        title = "$name ${devices.joinToString(separator = ",")} $prefix"
    )
}
