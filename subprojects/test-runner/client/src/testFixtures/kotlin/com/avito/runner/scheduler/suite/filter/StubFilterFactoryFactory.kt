package com.avito.runner.scheduler.suite.filter

import com.avito.android.runner.report.ReportFactory
import com.avito.android.runner.report.StubReportFactory
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.config.createStub

internal object StubFilterFactoryFactory {

    fun create(
        filter: InstrumentationFilterData = InstrumentationFilterData.createStub(),
        impactAnalysisResult: ImpactAnalysisResult = ImpactAnalysisResult.createStubInstance(),
        loggerFactory: LoggerFactory = StubLoggerFactory,
        reportFactory: ReportFactory = StubReportFactory()
    ): FilterFactory {
        return FilterFactory.create(
            filterData = filter,
            impactAnalysisResult = impactAnalysisResult,
            reportFactory = reportFactory,
            loggerFactory = loggerFactory
        )
    }
}
