package com.avito.instrumentation.internal.finalizer.action

import com.avito.instrumentation.internal.finalizer.TestRunResult
import com.avito.instrumentation.internal.finalizer.verdict.Verdict

internal interface FinalizeAction {

    fun action(testRunResult: TestRunResult, verdict: Verdict)
}
