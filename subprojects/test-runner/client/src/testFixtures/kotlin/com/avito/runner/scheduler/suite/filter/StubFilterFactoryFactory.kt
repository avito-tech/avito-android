package com.avito.runner.scheduler.suite.filter

import com.avito.logger.LoggerFactory
import com.avito.logger.PrintlnLoggerFactory
import com.avito.report.Report
import com.avito.report.StubReport
import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.config.createStub

internal object StubFilterFactoryFactory {

    fun create(
        filter: InstrumentationFilterData = InstrumentationFilterData.createStub(),
        impactAnalysisResult: ImpactAnalysisResult = ImpactAnalysisResult.createStubInstance(),
        loggerFactory: LoggerFactory = PrintlnLoggerFactory,
        report: Report = StubReport()
    ): FilterFactory {
        return FilterFactory.create(
            filterData = filter,
            impactAnalysisResult = impactAnalysisResult,
            report = report,
            loggerFactory = loggerFactory
        )
    }
}
