package com.avito.runner.scheduler.runner

import com.avito.runner.scheduler.runner.model.TestRunRequest

interface TestRunner {
    suspend fun runTests(tests: List<TestRunRequest>): TestRunnerResult
}
