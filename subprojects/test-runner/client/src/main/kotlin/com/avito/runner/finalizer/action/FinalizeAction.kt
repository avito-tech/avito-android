package com.avito.runner.finalizer.action

import com.avito.runner.finalizer.verdict.Verdict

internal interface FinalizeAction {

    fun action(verdict: Verdict)
}
