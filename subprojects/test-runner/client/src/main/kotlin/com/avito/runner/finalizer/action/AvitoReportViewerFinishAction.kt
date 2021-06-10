package com.avito.runner.finalizer.action

import com.avito.android.runner.report.LegacyReport
import com.avito.runner.finalizer.verdict.Verdict

internal class AvitoReportViewerFinishAction(
    private val legacyReport: LegacyReport
) : FinalizeAction {

    override fun action(verdict: Verdict) {
        if (verdict is Verdict.Failure) {
            if (verdict.notReportedTests.isNotEmpty()) {
                legacyReport.sendLostTests(verdict.notReportedTests)
            }
        }
        legacyReport.finish()
    }
}
