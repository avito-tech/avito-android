package com.avito.runner.scheduler.runner

import com.avito.runner.scheduler.runner.model.TestRunRequest
import kotlinx.coroutines.CoroutineScope

interface TestRunner {
    suspend fun runTests(tests: List<TestRunRequest>, scope: CoroutineScope): TestRunnerResult
}
