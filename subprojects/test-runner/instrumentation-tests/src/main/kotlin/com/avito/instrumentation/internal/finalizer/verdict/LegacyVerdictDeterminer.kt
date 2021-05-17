package com.avito.instrumentation.internal.finalizer.verdict

internal interface LegacyVerdictDeterminer {

    fun determine(
        failed: HasFailedTestDeterminer.Result,
        notReported: HasNotReportedTestsDeterminer.Result
    ): LegacyVerdict
}
