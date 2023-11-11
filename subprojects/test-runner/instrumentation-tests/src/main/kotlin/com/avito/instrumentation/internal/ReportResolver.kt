package com.avito.instrumentation.internal

import com.avito.android.plugins.configuration.RunIdResolver
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.instrumentation.configuration.report.ReportConfig

internal class ReportResolver(
    private val extension: InstrumentationTestsPluginExtension,
    private val runIdResolver: RunIdResolver
) {

    fun getRunId(): String {
        return when (extension.report.get()) {
            ReportConfig.NoOp -> ""
            is ReportConfig.ReportViewer -> runIdResolver.getRunId().toReportViewerFormat()
        }
    }

    fun getReport(): ReportConfig? {
        return extension.report.orNull
    }
}
