package com.avito.instrumentation.internal

import com.avito.cd.CdBuildResult
import com.avito.report.ReportLinksGenerator
import com.avito.reportviewer.model.ReportCoordinates

/**
 * Used for CD contract
 */
internal class GetTestResultsAction(
    private val reportCoordinates: ReportCoordinates,
    private val reportLinksGenerator: ReportLinksGenerator
) {

    fun getTestResults(): CdBuildResult.TestResultsLink {
        return CdBuildResult.TestResultsLink(
            reportUrl = reportLinksGenerator.generateReportLink(filterOnlyFailures = false),
            reportCoordinates = CdBuildResult.TestResultsLink.ReportCoordinates(
                planSlug = reportCoordinates.planSlug,
                jobSlug = reportCoordinates.jobSlug,
                runId = reportCoordinates.runId
            )
        )
    }
}
