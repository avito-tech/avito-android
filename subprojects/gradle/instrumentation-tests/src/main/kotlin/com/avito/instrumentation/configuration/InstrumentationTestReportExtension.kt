package com.avito.instrumentation.configuration

import org.gradle.api.Action

public class InstrumentationTestReportExtension {

    public class ReportViewer {
        public var reportApiUrl: String = ""
        public var reportViewerUrl: String = ""
        public var fileStorageUrl: String = ""

        internal fun validate() {
            require(reportApiUrl.isNotEmpty())
            require(reportViewerUrl.isNotEmpty())
            require(fileStorageUrl.isNotEmpty())
        }
    }

    internal var reportViewer: ReportViewer? = null

    public fun reportViewer(action: Action<ReportViewer>) {
        val reportViewer = ReportViewer()
        action.execute(reportViewer)
        reportViewer.validate()
        this.reportViewer = reportViewer
    }
}
