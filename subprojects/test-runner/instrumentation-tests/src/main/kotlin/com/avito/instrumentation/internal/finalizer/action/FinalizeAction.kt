package com.avito.instrumentation.internal.finalizer.action

import com.avito.instrumentation.internal.finalizer.verdict.Verdict

internal interface FinalizeAction {

    fun action(verdict: Verdict)
}
