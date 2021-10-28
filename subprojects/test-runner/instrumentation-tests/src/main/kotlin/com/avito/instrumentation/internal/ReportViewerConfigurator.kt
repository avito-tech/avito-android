package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask

internal class ReportViewerConfigurator(
    private val reportResolver: ReportResolver
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        val reportViewer = reportResolver.getReportViewer()
        if (reportViewer != null) {
            task.reportViewerProperty.set(reportViewer)
        }
    }
}
