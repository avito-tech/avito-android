package com.avito.android.runner.report.internal

import com.avito.android.runner.report.TestAttempt
import com.avito.report.model.AndroidTest

internal class OnlyLastExecutionMattersStrategy : TestAttemptsAggregateStrategy {

    override fun getTestResult(executions: Collection<TestAttempt>): AndroidTest {
        require(executions.isNotEmpty()) { "TestAttemptsAggregateStrategy called with no executions" }
        val sortedExecutions = executions.sortedBy { it.executionNumber }.map { it.testResult }
        return sortedExecutions.last()
    }
}
