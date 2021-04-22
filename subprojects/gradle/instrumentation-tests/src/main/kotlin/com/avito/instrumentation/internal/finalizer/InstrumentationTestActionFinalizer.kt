package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.scheduling.TestsScheduler

internal interface InstrumentationTestActionFinalizer {

    fun finalize(testsExecutionResults: TestsScheduler.Result)
}
