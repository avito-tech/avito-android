package com.avito.runner.finalizer.verdict

internal interface LegacyVerdictDeterminer {

    fun determine(
        failed: HasFailedTestDeterminer.Result,
        notReported: HasNotReportedTestsDeterminer.Result
    ): LegacyVerdict
}
