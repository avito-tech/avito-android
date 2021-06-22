package com.avito.runner.scheduler

import com.avito.report.model.TestStaticData
import com.avito.runner.scheduler.runner.TestRunner

internal interface TestRunnerFactory {
    fun createTestRunner(
        tests: List<TestStaticData>
    ): TestRunner
}
