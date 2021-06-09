package com.avito.runner.scheduler.runner

import com.avito.runner.scheduler.runner.model.TestRunRequest
import com.avito.runner.scheduler.runner.model.TestRunnerResult

internal class StubTestRunner(private val result: TestRunnerResult) : TestRunner {

    override suspend fun runTests(tests: List<TestRunRequest>): TestRunnerResult = result
}
