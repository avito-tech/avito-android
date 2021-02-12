package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.createStub
import com.avito.instrumentation.createStubInstance
import com.avito.instrumentation.internal.suite.filter.FilterFactory
import com.avito.instrumentation.internal.suite.filter.ImpactAnalysisResult
import com.avito.instrumentation.report.ReadReport
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.report.ReportFactory
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance

internal object FilterFactoryFactory {

    fun create(
        filter: InstrumentationFilter.Data = InstrumentationFilter.Data.createStub(),
        impactAnalysisResult: ImpactAnalysisResult = ImpactAnalysisResult.createStubInstance(),
        reportsByConfig: Map<ReportFactory.Config, Report> = emptyMap(),
        reportConfig: ReportFactory.Config = ReportFactory.Config.ReportViewerCoordinates(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "stub"
        )
    ): FilterFactory {
        return FilterFactory.create(
            filterData = filter,
            impactAnalysisResult = impactAnalysisResult,
            reportConfig = reportConfig,
            factory = object : ReportFactory {
                override fun createReport(config: ReportFactory.Config): Report {
                    TODO("Not yet implemented")
                }

                override fun createReadReport(config: ReportFactory.Config): ReadReport {
                    return reportsByConfig[config] ?: throw IllegalArgumentException("No report by config: $config")
                }
            }
        )
    }
}
