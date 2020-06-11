package com.avito.instrumentation.configuration

import org.gradle.api.Action

class InstrumentationTestReportExtension {

    class ReportViewer {
        var reportApiUrl: String = ""
        var reportApiFallbackUrl: String = ""
        var reportViewerUrl: String = ""
        var fileStorageUrl = ""

        internal fun validate() {
            require(reportApiUrl.isNotEmpty())
            require(reportApiFallbackUrl.isNotEmpty())
            require(reportViewerUrl.isNotEmpty())
            require(fileStorageUrl.isNotEmpty())
        }
    }

    internal var reportViewer: ReportViewer? = null

    fun reportViewer(action: Action<ReportViewer>) {
        val reportViewer = ReportViewer()
        action.execute(reportViewer)
        reportViewer.validate()
        this.reportViewer = reportViewer
    }
}
