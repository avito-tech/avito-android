package com.avito.instrumentation.internal

import com.avito.android.plugins.configuration.RunIdResolver
import com.avito.instrumentation.configuration.report.ReportConfig
import com.avito.instrumentation_args.InstrumentationArgsProvider

internal class ReportInstrumentationArgsProvider(
    private val reportResolver: ReportResolver,
    private val runIdResolver: RunIdResolver,
) : InstrumentationArgsProvider {

    override fun provideInstrumentationArgs(): Map<String, String> {
        val result = mutableMapOf<String, String>()

        when (val report = reportResolver.getReport() ?: ReportConfig.NoOp) {
            ReportConfig.NoOp -> result["avito.report.transport"] = "noop"
            is ReportConfig.ReportViewer.SendFromDevice -> {
                result["avito.report.transport"] = "backend"
                result["planSlug"] = report.planSlug
                result["jobSlug"] = report.jobSlug
                result["runId"] = runIdResolver.getRunId().toReportViewerFormat()
                result["fileStorageUrl"] = report.fileStorageUrl
                result["reportViewerUrl"] = report.reportViewerUrl
                result["reportApiUrl"] = report.reportApiUrl
                result["deviceName"] = "local"
            }
            is ReportConfig.ReportViewer.SendFromRunner -> {
                result["avito.report.transport"] = "legacy"
                result["fileStorageUrl"] = report.fileStorageUrl
            }
        }
        return result
    }
}
