package com.avito.runner.finalizer

import com.avito.runner.scheduler.runner.model.TestRunnerResults
import com.avito.runner.scheduler.runner.scheduler.TestSchedulerResult

internal interface Finalizer {

    fun finalize(testSchedulerResults: TestRunnerResults): TestSchedulerResult
}
