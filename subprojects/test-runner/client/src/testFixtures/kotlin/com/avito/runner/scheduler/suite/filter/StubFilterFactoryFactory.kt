package com.avito.runner.scheduler.suite.filter

import com.avito.android.runner.report.Report
import com.avito.android.runner.report.StubReport
import com.avito.logger.LoggerFactory
import com.avito.logger.StubLoggerFactory
import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.config.createStub

internal object StubFilterFactoryFactory {

    fun create(
        filter: InstrumentationFilterData = InstrumentationFilterData.createStub(),
        impactAnalysisResult: ImpactAnalysisResult = ImpactAnalysisResult.createStubInstance(),
        loggerFactory: LoggerFactory = StubLoggerFactory,
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
