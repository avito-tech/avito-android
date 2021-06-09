package com.avito.runner.scheduler.suite.filter

import com.avito.android.runner.report.ReportFactory
import com.avito.logger.LoggerFactory
import com.avito.runner.config.InstrumentationFilterData

internal interface FilterFactory {

    fun createFilter(): TestsFilter

    companion object {

        internal const val JUNIT_IGNORE_ANNOTATION = "org.junit.Ignore"

        fun create(
            filterData: InstrumentationFilterData,
            impactAnalysisResult: ImpactAnalysisResult,
            loggerFactory: LoggerFactory,
            reportFactory: ReportFactory
        ): FilterFactory {
            return FilterFactoryImpl(
                filterData = filterData,
                impactAnalysisResult = impactAnalysisResult,
                loggerFactory = loggerFactory,
                reportFactory = reportFactory
            )
        }
    }
}
