package com.avito.runner.finalizer.action

import com.avito.android.runner.report.LegacyReport
import com.avito.runner.finalizer.TestRunResult
import com.avito.runner.finalizer.verdict.HasNotReportedTestsDeterminer
import com.avito.runner.finalizer.verdict.LegacyVerdict

internal class LegacyAvitoReportViewerFinishAction(
    private val legacyReport: LegacyReport
) : LegacyFinalizeAction {

    override fun action(testRunResult: TestRunResult, verdict: LegacyVerdict) {
        if (testRunResult.notReported is HasNotReportedTestsDeterminer.Result.HasNotReportedTests) {
            val lostTests = testRunResult.notReported.lostTests
            legacyReport.sendLostTests(lostTests)
        }
        legacyReport.finish()
    }
}
