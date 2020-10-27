package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.report.Report

interface FilterFactory {

    fun createFilter(): TestsFilter

    companion object {
        
        internal const val JUNIT_IGNORE_ANNOTATION = "org.junit.Ignore"

        fun create(
            filterData: InstrumentationFilter.Data,
            impactAnalysisResult: ImpactAnalysisResult,
            reportConfig: Report.Factory.Config,
            factory: Report.Factory
        ): FilterFactory {
            return FilterFactoryImpl(
                filterData = filterData,
                impactAnalysisResult = impactAnalysisResult,
                reportConfig = reportConfig,
                factory = factory
            )
        }
    }
}
