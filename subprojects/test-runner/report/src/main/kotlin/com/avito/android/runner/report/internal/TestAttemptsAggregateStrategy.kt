package com.avito.android.runner.report.internal

import com.avito.android.runner.report.TestAttempt
import com.avito.report.model.AndroidTest

internal interface TestAttemptsAggregateStrategy {

    fun getTestResult(executions: Collection<TestAttempt>): AndroidTest
}
