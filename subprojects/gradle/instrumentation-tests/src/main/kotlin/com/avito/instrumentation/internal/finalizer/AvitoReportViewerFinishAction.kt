package com.avito.instrumentation.internal.finalizer

import com.avito.android.runner.report.LegacyReport
import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer.FinalizeAction

internal class AvitoReportViewerFinishAction(
    private val legacyReport: LegacyReport
) : FinalizeAction {

    override fun action(testRunResult: TestRunResult) {
        if (testRunResult.notReported is HasNotReportedTestsDeterminer.Result.HasNotReportedTests) {
            val lostTests = testRunResult.notReported.lostTests
            legacyReport.sendLostTests(lostTests)
        }
        legacyReport.finish()
    }
}
