package com.avito.instrumentation.internal.finalizer.action

import com.avito.instrumentation.internal.finalizer.TestRunResult
import com.avito.instrumentation.internal.finalizer.verdict.LegacyVerdict

internal interface LegacyFinalizeAction {

    fun action(
        testRunResult: TestRunResult,
        verdict: LegacyVerdict,
    )
}
