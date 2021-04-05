package com.avito.instrumentation.internal.finalizer

import com.avito.android.runner.report.Report
import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer.FinalizeAction
import com.avito.instrumentation.internal.report.HasNotReportedTestsDeterminer

internal class ReportFinishAction(
    private val report: Report
) : FinalizeAction {

    override fun action(testRunResult: TestRunResult) {
        if (testRunResult.notReported is HasNotReportedTestsDeterminer.Result.HasNotReportedTests) {
            val lostTests = testRunResult.notReported.lostTests
            report.sendLostTests(lostTests)
        }
        report.finish()
    }
}
