package com.avito.instrumentation

import com.avito.runner.scheduler.runner.model.TestSchedulerResult
import com.avito.runner.scheduler.runner.scheduler.TestScheduler

internal class StubTestScheduler : TestScheduler {

    var result = TestSchedulerResult(
        testsToRun = emptyList(),
        testResults = emptyList()
    )

    override fun schedule() = result
}
