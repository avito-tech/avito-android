package com.avito.instrumentation.internal.finalizer.action

import com.avito.instrumentation.internal.finalizer.TestRunResult
import com.avito.instrumentation.internal.finalizer.verdict.InstrumentationTestsTaskVerdict
import com.avito.instrumentation.internal.finalizer.verdict.Verdict
import com.avito.report.ReportLinkGenerator
import com.google.gson.Gson
import java.io.File

internal class WriteTaskVerdictAction(
    private val verdictDestination: File,
    private val gson: Gson,
    private val reportLinkGenerator: ReportLinkGenerator
) : FinalizeAction {

    override fun action(testRunResult: TestRunResult, verdict: Verdict) {
        val reportViewerUrl = reportLinkGenerator.generateReportLink()
        verdictDestination.writeText(
            gson.toJson(
                InstrumentationTestsTaskVerdict(
                    title = verdict.message,
                    reportUrl = reportViewerUrl,
                    causeFailureTests = verdict.getCauseFailureTests()
                )
            )
        )
    }

    private fun Verdict.getCauseFailureTests() =
        when (this) {
            is Verdict.Success -> emptySet()
            is Verdict.Failure -> {
                val failedTestsVerdict = prettifiedDetails.failedTests
                    .map { test -> test.toTaskVerdictTest("FAILED") }

                val lostTestsVerdict = prettifiedDetails.lostTests
                    .map { test -> test.toTaskVerdictTest("LOST") }
                (failedTestsVerdict + lostTestsVerdict).toSet()
            }
        }

    private fun Verdict.Failure.Details.Test.toTaskVerdictTest(
        prefix: String
    ): InstrumentationTestsTaskVerdict.Test = InstrumentationTestsTaskVerdict.Test(
        testUrl = reportLinkGenerator.generateTestLink(name),
        title = "$name ${devices.joinToString(separator = ",")} $prefix"
    )
}
