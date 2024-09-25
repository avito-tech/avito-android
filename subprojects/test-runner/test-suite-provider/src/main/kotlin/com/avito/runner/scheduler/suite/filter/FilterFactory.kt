package com.avito.runner.scheduler.suite.filter

import com.avito.logger.LoggerFactory
import com.avito.runner.scheduler.suite.config.InstrumentationFilterData
import com.avito.runner.scheduler.suite.filter.run_results_provider.RunResultsProvider

public interface FilterFactory {

    public fun createFilter(): TestsFilter

    public companion object {

        internal const val JUNIT_IGNORE_ANNOTATION = "org.junit.Ignore"

        public fun create(
            filterData: InstrumentationFilterData,
            impactAnalysisResult: ImpactAnalysisResult,
            loggerFactory: LoggerFactory,
            runResultsProvider: RunResultsProvider,
        ): FilterFactory {
            return FilterFactoryImpl(
                filterData = filterData,
                impactAnalysisResult = impactAnalysisResult,
                loggerFactory = loggerFactory,
                runResultsProvider = runResultsProvider,
            )
        }
    }
}
