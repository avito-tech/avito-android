package com.avito.runner.scheduler

import com.avito.runner.scheduler.runner.TestRunner

internal interface TestRunnerFactory {
    fun createTestRunner(): TestRunner
}
