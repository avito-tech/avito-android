package com.avito.runner.scheduler.suite.filter

import com.avito.logger.LoggerFactory
import com.avito.logger.PrintlnLoggerFactory
import com.avito.runner.scheduler.suite.config.InstrumentationFilterData
import com.avito.runner.scheduler.suite.config.createStub

internal object StubFilterFactoryFactory {

    fun create(
        filter: InstrumentationFilterData = InstrumentationFilterData.createStub(),
        impactAnalysisResult: ImpactAnalysisResult = ImpactAnalysisResult.createStubInstance(),
        loggerFactory: LoggerFactory = PrintlnLoggerFactory,
        runResultsProvider: RunResultsProvider = StubRunResultsProvider()
    ): FilterFactory {
        return FilterFactory.create(
            filterData = filter,
            impactAnalysisResult = impactAnalysisResult,
            runResultsProvider = runResultsProvider,
            loggerFactory = loggerFactory
        )
    }
}
