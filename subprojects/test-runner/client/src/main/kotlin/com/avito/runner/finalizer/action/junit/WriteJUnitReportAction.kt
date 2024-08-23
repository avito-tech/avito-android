package com.avito.runner.finalizer.action.junit

import com.avito.junit.Duration
import com.avito.junit.JUnitReportGenerator
import com.avito.junit.JunitReportTestCase
import com.avito.report.model.AndroidTest
import com.avito.runner.finalizer.action.FinalizeAction
import com.avito.runner.finalizer.verdict.Verdict
import java.io.File

internal class WriteJUnitReportAction(
    private val destination: File,
    private val jUnitReportGenerator: JUnitReportGenerator,
) : FinalizeAction {

    override fun action(verdict: Verdict) {
        val testCases: List<JunitReportTestCase> = when (verdict) {
            is Verdict.Success.OK ->
                verdict.testResults.map {
                    when (it) {
                        is AndroidTest.Completed -> it.success()
                        is AndroidTest.Skipped -> it.skipped()
                        is AndroidTest.Lost ->
                            throw IllegalStateException("Lost test can't be in Verdict.Success.OK")
                    }
                }

            is Verdict.Success.Suppressed ->
                verdict.testResults.map {
                    when (it) {
                        is AndroidTest.Completed ->
                            if (it.incident == null) it.success() else it.failed()

                        is AndroidTest.Lost -> it.failedToExecute()
                        is AndroidTest.Skipped -> it.skipped()
                    }
                } + verdict.notReportedTests.map { it.failedToExecute() }

            is Verdict.Failure ->
                verdict.testResults.map {
                    when (it) {
                        is AndroidTest.Completed ->
                            if (it.incident == null) it.success() else it.failed()

                        is AndroidTest.Lost -> it.failedToExecute()
                        is AndroidTest.Skipped -> it.skipped()
                    }
                } + verdict.notReportedTests.map { it.failedToExecute() }
        }

        val reportXml = jUnitReportGenerator.generateReport(
            testCases
        )

        destination.writeText(reportXml)
    }

    private fun AndroidTest.Completed.success(): JunitReportTestCase.Success = JunitReportTestCase.Success(
        name, testCaseId, Duration.Executed(duration)
    )

    private fun AndroidTest.Completed.failed(): JunitReportTestCase.Failed = JunitReportTestCase.Failed(
        name, testCaseId, Duration.Executed(duration), requireNotNull(incident) {
            "Failed map to Failed. incident == null"
        }.errorMessage
    )

    private fun AndroidTest.Skipped.skipped(): JunitReportTestCase.Skipped = JunitReportTestCase.Skipped(
        name, testCaseId, Duration.Unknown, skipReason
    )

    private fun AndroidTest.Lost.failedToExecute(): JunitReportTestCase.Error =
        JunitReportTestCase.Error(
            name, testCaseId, Duration.Unknown, incident?.errorMessage ?: "LOST (no info in report)"
        )
}
