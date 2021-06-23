package com.avito.android.runner.report.internal

import com.avito.android.runner.report.Report
import com.avito.android.runner.report.TestAttempt
import com.avito.logger.StubLoggerFactory
import com.avito.report.model.AndroidTest
import com.avito.report.model.Incident
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.createStubInstance
import com.avito.time.StubTimeProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class InMemoryReportTest {

    private val timeProvider = StubTimeProvider()
    private val loggerFactory = StubLoggerFactory
    private val aggregateStrategy: TestAttemptsAggregateStrategy = OnlyLastExecutionMattersStrategy()

    @Test
    fun `getTestResults - returns single success - single success attempt`() {
        val report = createInMemoryReport()

        val success = AndroidTest.Completed.createStubInstance()

        report.addTest(TestAttempt(testResult = success, 2))

        val result = report.getTestResults()

        assertThat(result).containsExactly(success)
    }

    @Test
    fun `getTestResults - returns single success - fail(lost) than success(completed)`() {
        val report = createInMemoryReport()

        val success = AndroidTest.Completed.createStubInstance()

        report.addTest(TestAttempt(testResult = AndroidTest.Lost.createStubInstance(), 1))
        report.addTest(TestAttempt(testResult = success, 2))

        val result = report.getTestResults()

        assertThat(result).containsExactly(success)
    }

    @Test
    fun `getTestResults - returns single success - fail(completed) than success(completed)`() {
        val report = createInMemoryReport()

        val success = AndroidTest.Completed.createStubInstance()

        report.addTest(
            TestAttempt(
                testResult = AndroidTest.Completed.createStubInstance(
                    testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                        incident = Incident.createStubInstance()
                    )
                ),
                executionNumber = 1
            )
        )
        report.addTest(TestAttempt(testResult = success, 2))

        val result = report.getTestResults()

        assertThat(result).containsExactly(success)
    }

    private fun createInMemoryReport(): Report = InMemoryReport(
        timeProvider = timeProvider,
        loggerFactory = loggerFactory,
        testAttemptsAggregateStrategy = aggregateStrategy
    )
}
