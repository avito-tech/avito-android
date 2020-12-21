package com.avito.runner.scheduler.runner

import com.avito.runner.scheduler.runner.model.TestRunRequest
import kotlinx.coroutines.CoroutineScope

class StubTestRunner(private val result: TestRunnerResult) : TestRunner {

    override suspend fun runTests(tests: List<TestRunRequest>, scope: CoroutineScope): TestRunnerResult = result
}
