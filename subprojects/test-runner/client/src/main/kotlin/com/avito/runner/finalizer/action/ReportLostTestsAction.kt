package com.avito.runner.finalizer.action

import com.avito.report.Report
import com.avito.runner.finalizer.verdict.Verdict

internal class ReportLostTestsAction(
    private val report: Report
) : FinalizeAction {

    override fun action(verdict: Verdict) {
        if (verdict is Verdict.Failure) {
            if (verdict.notReportedTests.isNotEmpty()) {
                report.reportLostTests(verdict.notReportedTests)
            }
        }
    }
}
