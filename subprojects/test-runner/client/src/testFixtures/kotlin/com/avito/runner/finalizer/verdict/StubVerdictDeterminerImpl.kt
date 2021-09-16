package com.avito.runner.finalizer.verdict

import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider

internal fun VerdictDeterminerImpl.Companion.createStubInstance(
    suppressFlaky: Boolean = false,
    suppressFailure: Boolean = false,
    timeProvider: TimeProvider = DefaultTimeProvider()
): VerdictDeterminerImpl {
    return VerdictDeterminerImpl(
        suppressFlaky = suppressFlaky,
        suppressFailure = suppressFailure,
        timeProvider = timeProvider,
    )
}
