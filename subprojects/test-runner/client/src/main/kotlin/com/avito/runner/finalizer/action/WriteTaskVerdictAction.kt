package com.avito.runner.finalizer.action

import com.avito.report.ReportLinkGenerator
import com.avito.report.model.TestStaticData
import com.avito.runner.finalizer.verdict.InstrumentationTestsTaskVerdict
import com.avito.runner.finalizer.verdict.Verdict
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

internal class WriteTaskVerdictAction(
    private val verdictDestination: File,
    private val reportLinkGenerator: ReportLinkGenerator,
) : FinalizeAction {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

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
