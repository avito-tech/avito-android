package com.avito.runner.scheduler.runner

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.TestRunnerResult

internal interface TestRunner {

    suspend fun runTests(
        tests: List<TestRunRequest>
    ): TestRunnerResult
}
