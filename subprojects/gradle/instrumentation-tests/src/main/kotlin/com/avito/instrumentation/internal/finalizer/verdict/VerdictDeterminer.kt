package com.avito.instrumentation.internal.finalizer.verdict

internal interface VerdictDeterminer {

    fun determine(
        failed: HasFailedTestDeterminer.Result,
        notReported: HasNotReportedTestsDeterminer.Result
    ): Verdict
}
