package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.instrumentation.configuration.ReportViewer

internal class ReportResolver(private val runIdResolver: RunIdResolver) {

    fun getReportApiUrl(extension: InstrumentationTestsPluginExtension): String {
        return extension.testReport.reportViewer?.reportApiUrl ?: "http://stub"
    }

    fun getReportViewerUrl(extension: InstrumentationTestsPluginExtension): String {
        return extension.testReport.reportViewer?.reportViewerUrl ?: "http://stub"
    }

    fun getRunId(extension: InstrumentationTestsPluginExtension): String {
        return runIdResolver.getCiRunId(extension).toReportViewerFormat()
    }

    fun getFileStorageUrl(extension: InstrumentationTestsPluginExtension): String {
        return extension.testReport.reportViewer?.fileStorageUrl ?: "http://stub"
    }

    fun getReportViewer(
        extension: InstrumentationTestsPluginExtension
    ): ReportViewer? {
        val reportViewer = extension.testReport.reportViewer
        return if (reportViewer != null) {
            ReportViewer(
                reportApiUrl = getReportApiUrl(extension),
                reportViewerUrl = getReportViewerUrl(extension),
                reportRunIdPrefix = reportViewer.reportRunIdPrefix,
                fileStorageUrl = getFileStorageUrl(extension)
            )
        } else {
            null
        }
    }
}
