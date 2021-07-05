package com.avito.android.runner.report

import com.avito.android.runner.report.internal.AvitoReport
import com.avito.android.runner.report.internal.InMemoryReport
import com.avito.android.runner.report.internal.OnlyLastExecutionMattersStrategy
import com.avito.android.runner.report.internal.ReportImpl
import com.avito.android.runner.report.internal.TestAttemptsAggregateStrategy
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.report.NoOpReportLinksGenerator
import com.avito.report.NoOpTestSuiteNameProvider
import com.avito.report.ReportLinksGenerator
import com.avito.report.TestSuiteNameProvider
import com.avito.reportviewer.ReportViewerLinksGeneratorImpl
import com.avito.reportviewer.ReportsApiFactory
import com.avito.time.TimeProvider

public class ReportFactoryImpl(
    private val timeProvider: TimeProvider,
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
                timeProvider = timeProvider,
                loggerFactory = loggerFactory,
                testAttemptsAggregateStrategy = createTestAttemptsAggregateStrategy()
            ),
            avitoReport = reportViewerConfig?.let { createAvitoReport(it) }
        )
    }

    private fun createAvitoReport(reportViewerConfig: ReportViewerConfig): AvitoReport {
        return AvitoReport(
            reportsApi = ReportsApiFactory.create(
                host = reportViewerConfig.apiUrl,
                loggerFactory = loggerFactory,
                httpClientProvider = httpClientProvider
            ),
            loggerFactory = loggerFactory,
            reportCoordinates = reportViewerConfig.reportCoordinates,
            buildId = buildId,
            timeProvider = timeProvider
        )
    }

    override fun createReportLinkGenerator(): ReportLinksGenerator {
        return if (reportViewerConfig != null) {
            ReportViewerLinksGeneratorImpl(
                reportViewerUrl = reportViewerConfig.viewerUrl,
                reportCoordinates = reportViewerConfig.reportCoordinates
            )
        } else {
            NoOpReportLinksGenerator()
        }
    }

    override fun createTestSuiteNameGenerator(): TestSuiteNameProvider {
        return if (reportViewerConfig != null) {
            ReportViewerLinksGeneratorImpl(
                reportViewerUrl = reportViewerConfig.viewerUrl,
                reportCoordinates = reportViewerConfig.reportCoordinates
            )
        } else {
            NoOpTestSuiteNameProvider()
        }
    }

    private fun createTestAttemptsAggregateStrategy(): TestAttemptsAggregateStrategy {
        return OnlyLastExecutionMattersStrategy()
    }
}
