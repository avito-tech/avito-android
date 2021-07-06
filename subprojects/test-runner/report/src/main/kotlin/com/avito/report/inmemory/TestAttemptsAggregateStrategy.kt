package com.avito.report.inmemory

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestAttempt

internal interface TestAttemptsAggregateStrategy {

    fun getTestResult(executions: Collection<TestAttempt>): AndroidTest
}
