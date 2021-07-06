package com.avito.runner.finalizer.action

import com.avito.report.NoOpReportLinksGenerator
import com.avito.report.ReportLinksGenerator
import java.io.File

internal fun WriteTaskVerdictAction.Companion.createStubInstance(
    verdictDestination: File,
    reportLinksGenerator: ReportLinksGenerator = NoOpReportLinksGenerator()
): WriteTaskVerdictAction {
    return WriteTaskVerdictAction(
        verdictDestination = verdictDestination,
        reportLinksGenerator = reportLinksGenerator
    )
}
