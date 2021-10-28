package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.instrumentation.configuration.ReportViewer

internal class ReportResolver(
    private val extension: InstrumentationTestsPluginExtension,
    private val runIdResolver: RunIdResolver
) {

    fun getReportApiUrl(): String {
        return extension.testReport.reportViewer?.reportApiUrl ?: "http://stub"
    }

    fun getReportViewerUrl(): String {
        return extension.testReport.reportViewer?.reportViewerUrl ?: "http://stub"
    }

    fun getRunId(): String {
        return runIdResolver.getCiRunId(extension).toReportViewerFormat()
    }

    fun getFileStorageUrl(): String {
        return extension.testReport.reportViewer?.fileStorageUrl ?: "http://stub"
    }

    fun getReportViewer(): ReportViewer? {
        val reportViewer = extension.testReport.reportViewer
        return if (reportViewer != null) {
            ReportViewer(
                reportApiUrl = getReportApiUrl(),
                reportViewerUrl = getReportViewerUrl(),
                reportRunIdPrefix = reportViewer.reportRunIdPrefix,
                fileStorageUrl = getFileStorageUrl()
            )
        } else {
            null
        }
    }
}
