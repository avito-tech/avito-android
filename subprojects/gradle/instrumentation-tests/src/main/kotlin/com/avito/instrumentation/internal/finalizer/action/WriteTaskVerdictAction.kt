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
                if (lostTests.isNotEmpty()) {
                    appendLine("Failed. There are ${lostTests.size} not reported tests")
                }

                if (failedTests.isNotEmpty()) {
                    appendLine("Failed. There are ${failedTests.size} not suppressed failed tests")
                }
            }
        }
    }

    private fun Verdict.getProblemTests(): Set<InstrumentationTestsTaskVerdict.Test> {
        return when (this) {
            is Verdict.Success ->
                emptySet()

            is Verdict.Failure ->
                failedTests.map { test -> test.toTaskVerdictTest("FAILED") }.toSet() +
                    lostTests.map { test -> test.toTaskVerdictTest("LOST") }.toSet()
        }
    }

    private fun TestStaticData.toTaskVerdictTest(prefix: String): InstrumentationTestsTaskVerdict.Test =
        InstrumentationTestsTaskVerdict.Test(
            testUrl = reportLinkGenerator.generateTestLink(name),
            title = "$name $device $prefix"
        )
}
