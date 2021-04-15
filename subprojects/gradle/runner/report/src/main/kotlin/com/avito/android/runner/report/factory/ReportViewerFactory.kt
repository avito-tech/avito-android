package com.avito.android.runner.report.factory

import com.avito.android.runner.report.AvitoReport
import com.avito.android.runner.report.LegacyReport
import com.avito.android.runner.report.ReadReport
import com.avito.android.runner.report.Report
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.report.ReportsApi
import com.avito.report.ReportsApiFactory
import com.avito.time.TimeProvider

public class ReportViewerFactory(
    private val reportApiUrl: String,
    private val loggerFactory: LoggerFactory,
    private val timeProvider: TimeProvider,
    private val httpClientProvider: HttpClientProvider
) : LegacyReportFactory {

    @Transient
    private lateinit var reportsApi: ReportsApi

    override fun createReport(config: LegacyReportFactory.Config): Report {
        return when (config) {
            is LegacyReportFactory.Config.ReportViewerCoordinates -> {
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

    override fun createLegacyReport(config: LegacyReportFactory.Config): LegacyReport {
        return when (config) {
            is LegacyReportFactory.Config.ReportViewerCoordinates -> {
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

    override fun createReadReport(config: LegacyReportFactory.Config): ReadReport {
        return when (config) {
            is LegacyReportFactory.Config.ReportViewerCoordinates -> {
                ensureInitializedReportsApi()
                ReadReport.ReportCoordinates(
                    reportsFetchApi = reportsApi,
                    coordinates = config.reportCoordinates
                )
            }
            is LegacyReportFactory.Config.ReportViewerId -> {
                ensureInitializedReportsApi()
                ReadReport.Id(
                    reportsFetchApi = reportsApi,
                    id = config.reportId
                )
            }
            is LegacyReportFactory.Config.InMemory -> TODO("Unsupported type")
        }
    }

    private fun throwUnsupportedConfigException(config: LegacyReportFactory.Config): Nothing {
        throw IllegalArgumentException("Unsupported config: $config")
    }

    private fun ensureInitializedReportsApi() {
        if (!::reportsApi.isInitialized) {
            reportsApi = ReportsApiFactory.create(
                host = reportApiUrl,
                loggerFactory = loggerFactory,
                httpClientProvider = httpClientProvider
            )
        }
    }
}
