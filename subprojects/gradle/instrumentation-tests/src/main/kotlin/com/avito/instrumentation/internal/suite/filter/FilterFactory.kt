package com.avito.instrumentation.internal.suite.filter

import com.avito.android.runner.report.ReportFactory
import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.logger.LoggerFactory

internal interface FilterFactory {

    fun createFilter(): TestsFilter

    companion object {

        internal const val JUNIT_IGNORE_ANNOTATION = "org.junit.Ignore"

        fun create(
            filterData: InstrumentationFilter.Data,
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
