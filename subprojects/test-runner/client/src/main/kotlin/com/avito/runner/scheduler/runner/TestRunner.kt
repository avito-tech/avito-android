package com.avito.runner.scheduler.runner

import com.avito.android.Result
import com.avito.runner.scheduler.runner.model.TestRunnerResult
import com.avito.runner.scheduler.runner.model.TestWithTarget

internal interface TestRunner {

    suspend fun runTests(
        tests: List<TestWithTarget>
    ): Result<TestRunnerResult>
}
