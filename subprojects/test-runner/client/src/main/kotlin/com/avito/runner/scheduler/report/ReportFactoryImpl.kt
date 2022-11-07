package com.avito.runner.scheduler.report

import com.avito.logger.LoggerFactory
import com.avito.report.Report
import com.avito.report.ReportFactory
import com.avito.report.inmemory.InMemoryReportFactory
import com.avito.reportviewer.ReportsApiFactory
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient

public class ReportFactoryImpl(
    private val timeProvider: TimeProvider,
    private val buildId: String,
    private val loggerFactory: LoggerFactory,
    private val httpClientBuilder: OkHttpClient.Builder,
    private val reportViewerConfig: ReportViewerConfig?
) : ReportFactory {

    override fun createReport(): Report {
        return createReportInternal()
    }

    private fun createReportInternal(): Report {
        return ReportImpl(
            inMemoryReport = InMemoryReportFactory(
                timeProvider = timeProvider,
                loggerFactory = loggerFactory,
            ).createReport(),
            externalReportService = reportViewerConfig?.let { createAvitoReport(it) }
        )
    }

    private fun createAvitoReport(reportViewerConfig: ReportViewerConfig): AvitoReport {
        return AvitoReport(
            reportsApi = ReportsApiFactory.create(
                host = reportViewerConfig.apiUrl,
                builder = httpClientBuilder
            ),
            reportViewerUrl = reportViewerConfig.viewerUrl,
            loggerFactory = loggerFactory,
            reportCoordinates = reportViewerConfig.reportCoordinates,
            buildId = buildId,
            timeProvider = timeProvider
        )
    }
}
