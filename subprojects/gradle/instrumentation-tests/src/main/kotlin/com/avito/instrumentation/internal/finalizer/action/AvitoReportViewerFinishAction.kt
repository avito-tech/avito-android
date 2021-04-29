package com.avito.instrumentation.internal.finalizer.action

import com.avito.android.runner.report.LegacyReport
import com.avito.instrumentation.internal.finalizer.TestRunResult
import com.avito.instrumentation.internal.finalizer.verdict.HasNotReportedTestsDeterminer
import com.avito.instrumentation.internal.finalizer.verdict.Verdict

internal class AvitoReportViewerFinishAction(
    private val legacyReport: LegacyReport
) : FinalizeAction {

    override fun action(testRunResult: TestRunResult, verdict: Verdict) {
        if (testRunResult.notReported is HasNotReportedTestsDeterminer.Result.HasNotReportedTests) {
            val lostTests = testRunResult.notReported.lostTests
            legacyReport.sendLostTests(lostTests)
        }
        legacyReport.finish()
    }
}
