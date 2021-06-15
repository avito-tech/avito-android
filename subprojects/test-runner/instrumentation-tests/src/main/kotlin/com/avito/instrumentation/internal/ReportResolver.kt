package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.configuration.ReportViewer

internal class ReportResolver(private val runIdResolver: RunIdResolver) {

    fun getReportApiUrl(extension: GradleInstrumentationPluginConfiguration): String {
        return extension.testReport.reportViewer?.reportApiUrl ?: "http://stub"
    }

    fun getReportViewerUrl(extension: GradleInstrumentationPluginConfiguration): String {
        return extension.testReport.reportViewer?.reportViewerUrl ?: "http://stub"
    }

    fun getRunId(extension: GradleInstrumentationPluginConfiguration): String {
        return runIdResolver.getCiRunId(extension).toReportViewerFormat()
    }

    fun getFileStorageUrl(extension: GradleInstrumentationPluginConfiguration): String {
        return extension.testReport.reportViewer?.fileStorageUrl ?: "http://stub"
    }

    fun getReportViewer(
        extension: GradleInstrumentationPluginConfiguration
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
