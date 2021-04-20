package com.avito.instrumentation.internal.finalizer

import com.avito.instrumentation.internal.TestRunResult
import com.avito.instrumentation.internal.scheduling.TestsScheduler

internal interface InstrumentationTestActionFinalizer {

    fun finalize(testsExecutionResults: TestsScheduler.Result)

    interface FinalizeAction {

        fun action(testRunResult: TestRunResult)
    }
}
