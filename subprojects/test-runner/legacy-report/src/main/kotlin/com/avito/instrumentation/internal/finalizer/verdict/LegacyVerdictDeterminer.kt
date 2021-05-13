package com.avito.instrumentation.internal.finalizer.verdict

public interface LegacyVerdictDeterminer {

    public fun determine(
        failed: HasFailedTestDeterminer.Result,
        notReported: HasNotReportedTestsDeterminer.Result
    ): LegacyVerdict
}
