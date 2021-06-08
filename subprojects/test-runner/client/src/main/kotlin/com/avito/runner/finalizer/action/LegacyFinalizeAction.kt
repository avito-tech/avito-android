package com.avito.runner.finalizer.action

import com.avito.runner.finalizer.TestRunResult
import com.avito.runner.finalizer.verdict.LegacyVerdict

internal interface LegacyFinalizeAction {

    fun action(
        testRunResult: TestRunResult,
        verdict: LegacyVerdict,
    )
}
