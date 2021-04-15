package com.avito.instrumentation.internal.suite.filter

import com.avito.android.runner.report.factory.LegacyReportFactory
import com.avito.instrumentation.configuration.InstrumentationFilter

internal interface FilterFactory {

    fun createFilter(): TestsFilter

    companion object {

        internal const val JUNIT_IGNORE_ANNOTATION = "org.junit.Ignore"

        fun create(
            filterData: InstrumentationFilter.Data,
            impactAnalysisResult: ImpactAnalysisResult,
            legacyReportConfig: LegacyReportFactory.Config,
            factoryLegacy: LegacyReportFactory
        ): FilterFactory {
            return FilterFactoryImpl(
                filterData = filterData,
                impactAnalysisResult = impactAnalysisResult,
                legacyReportConfig = legacyReportConfig,
                factoryLegacy = factoryLegacy
            )
        }
    }
}
