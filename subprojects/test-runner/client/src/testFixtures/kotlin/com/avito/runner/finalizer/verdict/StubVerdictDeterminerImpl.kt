package com.avito.runner.finalizer.verdict

import com.avito.time.StubTimeProvider
import com.avito.time.TimeProvider

internal fun VerdictDeterminerImpl.Companion.createStubInstance(
    suppressFlaky: Boolean = false,
    suppressFailure: Boolean = false,
    timeProvider: TimeProvider = StubTimeProvider()
): VerdictDeterminerImpl {
    return VerdictDeterminerImpl(
        suppressFlaky = suppressFlaky,
        suppressFailure = suppressFailure,
        timeProvider = timeProvider,
    )
}
