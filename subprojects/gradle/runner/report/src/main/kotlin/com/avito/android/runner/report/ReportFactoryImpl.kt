package com.avito.android.runner.report

import com.avito.android.runner.report.internal.AvitoReport
import com.avito.android.runner.report.internal.InMemoryReport
import com.avito.android.runner.report.internal.ReportImpl
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.report.ReportLinkGenerator
import com.avito.report.ReportViewer
import com.avito.report.ReportsApiFactory
import com.avito.report.TestSuiteNameProvider
import com.avito.time.TimeProvider

public class ReportFactoryImpl(
    private val timeProvider: TimeProvider,
    private val useInMemoryReport: Boolean,
    private val buildId: String,
    private val loggerFactory: LoggerFactory,
    private val httpClientProvider: HttpClientProvider,
    private val reportViewerConfig: ReportViewerConfig?
) : ReportFactory {

    override fun createReport(): Report {
        return createReportInternal()
    }

    override fun createAvitoReport(): LegacyReport {
        return createAvitoReport(reportViewerConfig!!)
    }

    private fun createReportInternal(): Report {
        return ReportImpl(
            inMemoryReport = InMemoryReport(
                timeProvider = timeProvider
            ),
            avitoReport = reportViewerConfig?.let { createAvitoReport(it) },
            useInMemoryReport = useInMemoryReport
        )
    }

    private fun createAvitoReport(reportViewerConfig: ReportViewerConfig): AvitoReport {
        return AvitoReport(
            reportsApi = ReportsApiFactory.create(
                host = reportViewerConfig.url,
                loggerFactory = loggerFactory,
                httpClientProvider = httpClientProvider
            ),
            loggerFactory = loggerFactory,
            reportCoordinates = reportViewerConfig.reportCoordinates,
            buildId = buildId,
            timeProvider = timeProvider
        )
    }

    override fun createReportLinkGenerator(): ReportLinkGenerator {
        return if (reportViewerConfig != null) {
            ReportViewer.Impl(
                host = reportViewerConfig.url,
                reportCoordinates = reportViewerConfig.reportCoordinates
            )
        } else {
            ReportLinkGenerator.NoOp()
        }
    }

    override fun createTestSuiteNameGenerator(): TestSuiteNameProvider {
        return if (reportViewerConfig != null) {
            ReportViewer.Impl(
                host = reportViewerConfig.url,
                reportCoordinates = reportViewerConfig.reportCoordinates
            )
        } else {
            TestSuiteNameProvider.NoOp()
        }
    }
}
