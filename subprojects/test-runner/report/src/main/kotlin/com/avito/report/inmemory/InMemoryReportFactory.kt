package com.avito.report.inmemory

import com.avito.logger.LoggerFactory
import com.avito.report.NoOpReportLinksGenerator
import com.avito.report.NoOpTestSuiteNameProvider
import com.avito.report.Report
import com.avito.report.ReportFactory
import com.avito.time.TimeProvider

public class InMemoryReportFactory(
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory
) : ReportFactory {

    override fun createReport(): Report {
        return InMemoryReport(
            timeProvider = timeProvider,
            loggerFactory = loggerFactory,
            testAttemptsAggregateStrategy = OnlyLastExecutionMattersStrategy(),
            reportLinksGenerator = NoOpReportLinksGenerator(),
            testSuiteNameProvider = NoOpTestSuiteNameProvider()
        )
    }
}
