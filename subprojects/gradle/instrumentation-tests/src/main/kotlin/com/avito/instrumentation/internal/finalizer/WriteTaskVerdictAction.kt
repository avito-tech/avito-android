package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer.FinalizeAction
import com.avito.instrumentation.internal.verdict.InstrumentationTestsTaskVerdict
import com.avito.report.ReportLinkGenerator
import com.google.gson.Gson
import java.io.File

internal class WriteTaskVerdictAction(
    private val verdictDestination: File,
    private val gson: Gson,
    private val reportLinkGenerator: ReportLinkGenerator
) : FinalizeAction {

    override fun action(testRunResult: TestRunResult) {
        val reportViewerUrl = reportLinkGenerator.generateReportLink()
        verdictDestination.writeText(
            gson.toJson(
                InstrumentationTestsTaskVerdict(
                    title = testRunResult.verdict.message,
                    reportUrl = reportViewerUrl,
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
        testUrl = reportLinkGenerator.generateTestLink(name),
        title = "$name ${devices.joinToString(separator = ",")} $prefix"
    )
}
