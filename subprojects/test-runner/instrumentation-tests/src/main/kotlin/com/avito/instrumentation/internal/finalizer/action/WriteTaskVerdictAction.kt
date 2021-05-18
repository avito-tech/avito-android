package com.avito.instrumentation.internal.finalizer.action

import com.avito.instrumentation.internal.finalizer.verdict.InstrumentationTestsTaskVerdict
import com.avito.instrumentation.internal.finalizer.verdict.Verdict
import com.avito.report.ReportLinkGenerator
import com.avito.report.model.TestStaticData
import com.google.gson.Gson
import java.io.File

internal class WriteTaskVerdictAction(
    private val verdictDestination: File,
    private val gson: Gson,
    private val reportLinkGenerator: ReportLinkGenerator,
) : FinalizeAction {

    override fun action(verdict: Verdict) {
        verdictDestination.writeText(
            gson.toJson(
                InstrumentationTestsTaskVerdict(
                    title = verdict.message(),
                    reportUrl = reportLinkGenerator.generateReportLink(
                        filterOnlyFailtures = verdict is Verdict.Failure
                    ),
                    problemTests = verdict.getProblemTests()
                )
            )
        )
    }

    private fun Verdict.message(): String {
        return when (this) {
            is Verdict.Success.OK ->
                "OK. No failed tests"

            is Verdict.Success.Suppressed ->
                "OK. Failed tests were suppressed"

            is Verdict.Failure -> buildString {
                appendLine("Test suite failed. Problems:")
                appendLine()

                if (notReportedTests.isNotEmpty()) {
                    appendLine(" - Not reported tests: ${notReportedTests.size} ")
                }

                if (unsuppressedFailedTests.isNotEmpty()) {
                    appendLine(" - Not suppressed failed tests: ${unsuppressedFailedTests.size} ")
                }
            }
        }
    }

    private fun Verdict.getProblemTests(): Set<InstrumentationTestsTaskVerdict.Test> {
        return when (this) {
            is Verdict.Success ->
                emptySet()

            is Verdict.Failure ->
                unsuppressedFailedTests.map { test -> test.toTaskVerdictTest("FAILED") }.toSet() +
                    notReportedTests.map { test -> test.toTaskVerdictTest("NOT REPORTED") }.toSet()
        }
    }

    private fun TestStaticData.toTaskVerdictTest(prefix: String): InstrumentationTestsTaskVerdict.Test =
        InstrumentationTestsTaskVerdict.Test(
            testUrl = reportLinkGenerator.generateTestLink(name),
            title = "$name $device $prefix"
        )
}
