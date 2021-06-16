package com.avito.runner.finalizer.action

import com.avito.report.NoOpReportLinkGenerator
import com.avito.report.ReportLinkGenerator
import java.io.File

internal fun WriteTaskVerdictAction.Companion.createStubInstance(
    verdictDestination: File,
    reportLinkGenerator: ReportLinkGenerator = NoOpReportLinkGenerator()
): WriteTaskVerdictAction {
    return WriteTaskVerdictAction(
        verdictDestination = verdictDestination,
        reportLinkGenerator = reportLinkGenerator
    )
}
