package com.avito.report.inmemory

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestAttempt

internal class OnlyLastExecutionMattersStrategy : TestAttemptsAggregateStrategy {

    override fun getTestResult(executions: Collection<TestAttempt>): AndroidTest {
        require(executions.isNotEmpty()) { "TestAttemptsAggregateStrategy called with no executions" }
        val sortedExecutions = executions.sortedBy { it.executionNumber }.map { it.testResult }
        return sortedExecutions.last()
    }
}
