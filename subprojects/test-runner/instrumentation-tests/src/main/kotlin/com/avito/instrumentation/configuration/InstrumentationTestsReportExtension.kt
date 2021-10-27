package com.avito.instrumentation.configuration

import groovy.lang.Closure
import org.gradle.api.Action

public class InstrumentationTestsReportExtension {

    public class ReportViewer {
        public var reportApiUrl: String = ""
        public var reportViewerUrl: String = ""
        public var reportRunIdPrefix: String = ""
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

    public fun reportViewer(closure: Closure<ReportViewer>) {
        val reportViewer = ReportViewer()
        closure.delegate = reportViewer
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
        this.reportViewer = reportViewer
    }
}
