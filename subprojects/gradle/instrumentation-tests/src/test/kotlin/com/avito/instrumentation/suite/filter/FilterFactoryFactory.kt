package com.avito.instrumentation.suite.filter

import com.avito.android.runner.report.LegacyReport
import com.avito.android.runner.report.ReadReport
import com.avito.android.runner.report.Report
import com.avito.android.runner.report.factory.LegacyReportFactory
import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.createStub
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.internal.suite.filter.FilterFactory
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance

internal object FilterFactoryFactory {

    fun create(
        filter: InstrumentationFilter.Data = InstrumentationFilter.Data.createStub(),
        impactAnalysisResult: ImpactAnalysisResult = ImpactAnalysisResult.createStubInstance(),
        reportsByConfig: Map<LegacyReportFactory.Config, Report> = emptyMap(),
        legacyReportConfig: LegacyReportFactory.Config = LegacyReportFactory.Config.ReportViewerCoordinates(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "stub"
        )
    ): FilterFactory {
        return FilterFactory.create(
            filterData = filter,
            impactAnalysisResult = impactAnalysisResult,
            legacyReportConfig = legacyReportConfig,
            factoryLegacy = object : LegacyReportFactory {
                override fun createReport(config: LegacyReportFactory.Config): Report {
                    TODO("Not yet implemented")
                }

                override fun createLegacyReport(config: LegacyReportFactory.Config): LegacyReport {
                    TODO("Not yet implemented")
                }

                override fun createReadReport(config: LegacyReportFactory.Config): ReadReport {
                    return reportsByConfig[config] ?: throw IllegalArgumentException("No report by config: $config")
                }
            }
        )
    }
}
