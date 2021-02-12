package com.avito.android.runner.report.factory

import com.avito.android.runner.report.AvitoReport
import com.avito.android.runner.report.ReadReport
import com.avito.android.runner.report.Report
import com.avito.logger.LoggerFactory
import com.avito.report.ReportsApi
import com.avito.time.TimeProvider

public class ReportViewerFactory(
    public val reportApiUrl: String,
    public val loggerFactory: LoggerFactory,
    public val timeProvider: TimeProvider,
    public val verboseHttp: Boolean
) : ReportFactory {

    @Transient
    private lateinit var reportsApi: ReportsApi

    override fun createReport(config: ReportFactory.Config): Report {
        return when (config) {
            is ReportFactory.Config.ReportViewerCoordinates -> {
                ensureInitializedReportsApi()
                AvitoReport(
                    reportsApi = reportsApi,
                    loggerFactory = loggerFactory,
                    reportCoordinates = config.reportCoordinates,
                    buildId = config.buildId,
                    timeProvider = timeProvider
                )
            }
            else -> throwUnsupportedConfigException(config)
        }
    }

    override fun createReadReport(config: ReportFactory.Config): ReadReport {
        return when (config) {
            is ReportFactory.Config.ReportViewerCoordinates -> {
                ensureInitializedReportsApi()
                ReadReport.ReportCoordinates(
                    reportsFetchApi = reportsApi,
                    coordinates = config.reportCoordinates
                )
            }
            is ReportFactory.Config.ReportViewerId -> {
                ensureInitializedReportsApi()
                ReadReport.Id(
                    reportsFetchApi = reportsApi,
                    id = config.reportId
                )
            }
            is ReportFactory.Config.InMemory -> TODO("Unsupported type")
        }
    }

    private fun throwUnsupportedConfigException(config: ReportFactory.Config): Nothing {
        throw IllegalArgumentException("Unsupported config: $config")
    }

    private fun ensureInitializedReportsApi() {
        if (!::reportsApi.isInitialized) {
            reportsApi = ReportsApi.create(
                host = reportApiUrl,
                loggerFactory = loggerFactory,
                verboseHttp = verboseHttp
            )
        }
    }
}
